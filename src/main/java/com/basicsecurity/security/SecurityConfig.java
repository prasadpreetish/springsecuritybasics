package com.basicsecurity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private UserDetailsService userDetailsService; // Our CustomUserDetailsService
    private JwtAuthEntryPoint authenticationEntryPoint; // Our custom entry point
    private JwtTokenAuthenticationFilter authenticationFilter; // Our custom JWT filter

    // Constructor for dependency injection
    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtAuthEntryPoint authenticationEntryPoint,
                          JwtTokenAuthenticationFilter authenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationFilter = authenticationFilter;
    }

    // Bean to provide password encoding (using BCrypt)
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean to get the AuthenticationManager, which handles user authentication
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // This is the core of our security configuration
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)) // Handle unauthorized access
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions (JWT)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll() // Allow GET requests to all /api endpoints without auth (e.g. for public data)
                        .requestMatchers("/api/auth/**").permitAll() // Allow authentication endpoints (login, register) without auth
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                        .anyRequest().authenticated()); // All other requests require authentication

        // Add our custom JWT authentication filter before Spring's default UsernamePasswordAuthenticationFilter
        // This ensures our filter intercepts and validates JWTs first.
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Important for H2 Console access with Spring Security:
        // Spring Security by default disables frames to prevent clickjacking attacks.
        // H2 console uses frames, so we must explicitly enable them.
        // .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)); // For Spring Security 6+
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // For Spring Security 6+

        return http.build(); // Build and return the configured HttpSecurity object
    }
}
