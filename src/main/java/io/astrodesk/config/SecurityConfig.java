package io.astrodesk.config;

import io.astrodesk.user.LdapUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.base}")
    private String ldapBase;

    @Value("${ldap.dn}")
    private String ldapDn;

    @Value("${ldap.pwd}")
    private String ldapPwd;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager ldapAuthManager,
                                                   LdapUserService ldapUserService) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().fullyAuthenticated())
                .formLogin(form -> form
                        .successHandler((HttpServletRequest request,
                                         HttpServletResponse response,
                                         org.springframework.security.core.Authentication authentication) -> {
                            ldapUserService.syncUserOnLogin();
                            response.sendRedirect("/");
                        })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(ldapAuthManager);
        return http.build();
    }

    @Bean
    public AuthenticationManager ldapAuthManager(LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(ldapDn);
        contextSource.setPassword(ldapPwd);
        contextSource.afterPropertiesSet();

        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns("uid={0},ou=users");
        factory.setLdapAuthoritiesPopulator(ldapAuthoritiesPopulator);

        return factory.createAuthenticationManager();
    }

    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator(DefaultSpringSecurityContextSource contextSource) {
        DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(contextSource, "ou=groups");
        populator.setGroupRoleAttribute("cn");
        populator.setSearchSubtree(true);
        return populator;

    }
    @Bean
    public DefaultSpringSecurityContextSource contextSource() {
        DefaultSpringSecurityContextSource contextSource =
                new DefaultSpringSecurityContextSource(ldapUrl + "/" + ldapBase);
        contextSource.setUserDn(ldapDn);
        contextSource.setPassword(ldapPwd);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(ldapDn);
        contextSource.setPassword(ldapPwd);
        contextSource.afterPropertiesSet();
        return new LdapTemplate(contextSource);
    }
}
