package com.basicsecurity.controller;

import com.basicsecurity.model.Role;
import com.basicsecurity.model.User;
import com.basicsecurity.payload.JwtAuthResponse;
import com.basicsecurity.payload.LoginDto;
import com.basicsecurity.payload.RegisterDto;
import com.basicsecurity.repository.RoleRepository;
import com.basicsecurity.repository.UserRepository;
import com.basicsecurity.security.JwtTokenProvider;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthenticationManager authenticationManager; // For login authentication
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder; // for password encoding (hashing)
    private JwtTokenProvider jwtTokenProvider; // for generating JWT tokens

    // Constructor injection for all dependencies
    // Spring will automatically inject the appropriate beans (e.g., our CustomUserDetailsService for AuthenticationManager)
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Endpoint for user login
    // post // /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto){
        //Authenticate user credentials using AuthenticationManager
        // This manager uses our CustomUserDetailsService class to load user details
        // and then the configured PasswordEncoder to verify the password

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.usernameOrEmail(),loginDto.password()));


        // If authentication is successfull, set the Authentication object in SecurityContextHolder
        // This is crucial for Spring Security's context for the remainder of the requests;
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate the JWT Token now,
        String token = jwtTokenProvider.generateToken(authentication);

        // Return the JWT Token in Response;
        // Using the overloaded constructor of JwtAuthResponse record;

        return ResponseEntity.ok(new JwtAuthResponse(token));
    }

    // Endpoint for user registration
    // POST // /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto){
        if (userRepository.existsByUsername(registerDto.username())) { // Access components of record DTOs
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerDto.email())) { // Access components of record DTOs
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // Create new User object from DTO data;
        // Create new User object from DTO data
        User user = new User();
        user.setName(registerDto.name());
        user.setUsername(registerDto.username());
        user.setEmail(registerDto.email());
        // Encode the password before saving it to the database
        user.setPassword(passwordEncoder.encode(registerDto.password()));

        // Assign a default role (e.g., ROLE_USER) to the new user
        // We assume 'ROLE_USER' exists in the database by this point (e.g., via DataInitializer)
        Role roles = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_USER not found. Please ensure it's initialized in the database."));
        user.setRoles(Collections.singleton(roles)); // Assign only the "ROLE_USER" role for now

        // Save the new user to the database
        userRepository.save(user);

        return new ResponseEntity<>("User registered successfully!", HttpStatus.OK);
    }
}
