package com.basicsecurity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsService userDetailsService;

    // Constructor for dependency injection (Spring will autowire these)
    // Spring will look for a bean of type JwtTokenProvider and UserDetailsService
    public JwtTokenAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Get JWT token from HTTP request
        String token = getTokenFromRequest(request);

        // 2. Validate token
        // Checks if token is not null/empty and is valid according to JwtTokenProvider
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 3. Get username from token
            String username = jwtTokenProvider.getUsername(token);

            // 4. Load user associated with token (from our UserDetailsService)
            // This fetches UserDetails including authorities from the database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Create Authentication object
            // This object represents the authenticated user in Spring Security's context
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, // The UserDetails object (contains username, roles)
                    null,        // Credentials (null for JWT as they are already validated by the token)
                    userDetails.getAuthorities() // User's roles/authorities
            );

            // Set web authentication details (optional, but good practice for logging/auditing)
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6. Set Spring Security authentication in the SecurityContext
            // This is the crucial step that tells Spring Security:
            // "For this current request, this user is authenticated and has these roles."
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // Continue with the filter chain.
        // This passes the request to the next filter in Spring Security's chain (e.g., authorization filter)
        // or eventually to your controller.
        filterChain.doFilter(request, response);
    }


    // Helper method to extract JWT from "Authorization: Bearer <token>" header
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Get the full header value
        // Check if it's not empty and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Extract the token string (remove "Bearer ")
        }
        return null; // No Bearer token found
    }
}
