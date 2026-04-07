package io.astrodesk.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LdapUserService ldapUserService;
    private final UserMapper userMapper;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository;

    public UserService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       LdapUserService ldapUserService, UserMapper userMapper, SecurityContextRepository securityContextRepository) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.ldapUserService = ldapUserService;
        this.userMapper = userMapper;
        this.securityContextRepository = securityContextRepository;
    }

    public UserDTO authorizeLogin(UserLoginRequest userLoginRequest, HttpServletResponse response, HttpServletRequest request) {
        Authentication auth = new UsernamePasswordAuthenticationToken(userLoginRequest.username(), userLoginRequest.password());
        Authentication authenticated = authenticationManager.authenticate(auth);

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authenticated);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        ldapUserService.syncUserOnLogin();

        DbUserEntity user = userRepository.findByUsername(userLoginRequest.username()).orElseThrow();

        return userMapper.toDTO(user);
    }

    public void authorizeLogout(HttpServletResponse response, HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if(session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
