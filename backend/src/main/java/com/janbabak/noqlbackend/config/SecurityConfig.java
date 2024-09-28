package com.janbabak.noqlbackend.config;

import com.janbabak.noqlbackend.authentication.JwtAuthenticationFilter;
import com.janbabak.noqlbackend.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true) //enables @Secured annotation
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    //paths, which don't require authentication
    private final String[] noAuthPaths = {
            "/",
            "/auth/**",
            "static/images/**",
    };

    //paths, which require role ADMIN
    private final String[] adminPaths = {
            "/settings/**",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(noAuthPaths).permitAll()
                        .requestMatchers(adminPaths).hasAuthority(Role.ADMIN.name())
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(401, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(403, "Forbidden")));

        return httpSecurity.build();
    }
}
