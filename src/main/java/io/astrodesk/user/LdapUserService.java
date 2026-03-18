package io.astrodesk.user;

import javax.naming.directory.Attributes;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class LdapUserService {
    private final UserRepository userRepository;
    private final LdapTemplate ldapTemplate;

    public LdapUserService(UserRepository userRepository, LdapTemplate ldapTemplate) {
        this.userRepository = userRepository;
        this.ldapTemplate = ldapTemplate;
    }

    public void syncUserOnLogin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            System.out.println("Syncing user: " + username);
            if (userRepository.existsByUsername(username)) {
                System.out.println("User already exists: " + username);
                return;
            }
            String cn = ldapTemplate.lookup("uid=" + username + ",ou=users", (Attributes attrs) -> {
                try {
                    return (String) attrs.get("cn").get();
                } catch (NamingException e) {
                    return username;
                }
            });
            String email = ldapTemplate.lookup("uid=" + username + ",ou=users", (Attributes attrs) -> {
                try {
                    return (String) attrs.get("mail").get();
                } catch (NamingException e) {
                    return username + "@astrodesk";
                }
            });
            String fullName = cn != null ? cn : username;
            String firstName = fullName.contains(" ") ? fullName.split(" ")[0] : username;
            String lastName = fullName.contains(" ") ? fullName.split(" ")[1] : "";
            UserRole role = determineRole(auth.getAuthorities());
            DbUserEntity newUser = new DbUserEntity(
                    null,
                    username,
                    firstName,
                    lastName,
                    email,
                    role
            );
            userRepository.save(newUser);
            System.out.println("Created user in DB: " + username);
        } catch (Exception e) {
            System.out.println("Error syncing user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UserRole determineRole(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority auth : authorities) {
            String role = auth.getAuthority().toUpperCase();
            if (role.contains("ADMINS")) return UserRole.ADMIN;
            if (role.contains("DEVS")) return UserRole.DEV;
        }
        return UserRole.USER;
    }
}
