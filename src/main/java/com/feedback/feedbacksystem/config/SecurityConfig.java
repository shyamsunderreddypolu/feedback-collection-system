package com.feedback.feedbacksystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web security for the REST API.
 *
 * <p>Spring Security secures every request as soon as it is on the classpath, so without
 * this chain the API answers 401 with a generated password. The rules below keep the
 * endpoints open while the authentication module is still being built.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // The API is called with a bearer token rather than a session cookie, so
                // CSRF tokens do not apply and would only reject every POST and PUT.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // TODO: once /api/auth issues JWTs, add the token filter here and replace
                // permitAll with role rules, e.g. form writes restricted to ROLE_ADMIN.
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .build();
    }
}
