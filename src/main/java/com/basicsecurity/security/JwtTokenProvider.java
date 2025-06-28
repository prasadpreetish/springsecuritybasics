package com.basicsecurity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Inject JWT secret key from application.properties
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // Inject JWT expiration in milliseconds from application.properties
    @Value("${app.jwt-app-expiration-milliseconds}")
    private long jwtExpirationDate;


    // Helper method to get the signing key
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    // Generate JWT token
    public String generateToken(Authentication authentication){
        String username = authentication.getName();

        Date currentDate = new Date();
        Date expiryDate = new Date(currentDate.getTime()+jwtExpirationDate);

        String token = Jwts.builder()
                .setSubject(username)  // Subject of the token (the user)
                .setIssuedAt(new Date()) // When the token was issued
                .setExpiration(expiryDate) // When the token expires
                .signWith(key()) // Sign the token with our secret key
                .compact(); // Build and compact the token into a string


        return token;
    }

    // Get username from JWT token
    public String getUsername(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(key()) // Use the same key to parse
                .build()
                .parseClaimsJws(token) // parse the token
                .getBody(); // get the body of claims

        return claims.getSubject(); // subject was the username as we set above;
    }


    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(key()) // Use the key to validate;
                    .build()
                    .parseClaimsJws(token);
            return true; // if no exception then token is valid;
        }catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false; // Token is invalid
    }



}
