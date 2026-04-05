package io.astrodesk.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LdapUserService ldapUserService;
    private final UserMapper userMapper;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository;

    public UserController(@Qualifier("ldapAuthManager") AuthenticationManager authenticationManager, UserRepository userRepository, 
                          LdapUserService ldapUserService, UserMapper userMapper, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.ldapUserService = ldapUserService;
        this.userMapper = userMapper;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserLoginRequest userLoginRequest,
                                         HttpServletResponse response, HttpServletRequest request) {

        Authentication auth = new UsernamePasswordAuthenticationToken(userLoginRequest.username(), userLoginRequest.password());
        Authentication authenticated = authenticationManager.authenticate(auth);

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authenticated);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        ldapUserService.syncUserOnLogin();

        DbUserEntity user = userRepository.findByUsername(userLoginRequest.username()).orElseThrow();
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        if(request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setPath("/login");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }
}
