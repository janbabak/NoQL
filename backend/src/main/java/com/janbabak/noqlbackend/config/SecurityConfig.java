package com.janbabak.noqlbackend.config;

import com.janbabak.noqlbackend.authentication.JwtAuthenticationFilter;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    private final String[] adminPaths = {};

//    //enables cors for front end
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:5173"));
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
//        configuration.setAllowedHeaders(List.of("Authorization", "content-type"));
//        configuration.setExposedHeaders(List.of("Authorization", "content-type"));
//
//        // If you need to allow credentials
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(noAuthPaths).permitAll()
                                .requestMatchers(adminPaths).hasAuthority("ADMIN")
                                .anyRequest().authenticated())
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();

//                    .cors() !!
//                .and()
//                    .csrf() - ok
//                    .disable() - ok
//                    .authorizeHttpRequests() - ok
//                    .requestMatchers(noAuthPaths) - ok
//                    .permitAll() - ok
//                    .requestMatchers(adminPaths) - ok
//                    .hasAuthority(Role.ADMIN.name()) - ok
//                    .anyRequest() - ok
//                    .authenticated() - ok
//                .and()
//                    .sessionManagement() - ok
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //not saves session - ok
//                .and()
//                    .authenticationProvider(authenticationProvider) //set custom user details service  - ok
//                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) - ok
//                    .exceptionHandling() !!
//                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) !!
//                    .accessDeniedHandler(new CustomAccessDeniedHandler()); !!

    }
}
