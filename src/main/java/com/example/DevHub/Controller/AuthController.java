package com.example.DevHub.Controller;

import com.example.DevHub.Model.User;
import com.example.DevHub.Service.JwtService;
import com.example.DevHub.Service.UserService;
import com.example.DevHub.dto.LoginRequest; // Import external DTO
import com.example.DevHub.dto.RegisterRequest; // Import external DTO
import com.example.DevHub.exception.DuplicateEntryException; // Custom exception
import jakarta.validation.Valid; // For @Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException; // Specific authentication exception
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Logger
import org.slf4j.LoggerFactory; // Logger Factory

import java.util.HashMap;
import java.util.List; // For List type if used explicitly
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class); // Initialize logger

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param loginRequest The login credentials
     * @return ResponseEntity with JWT token or error
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) { // Add @Valid
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            User user = (User) authentication.getPrincipal(); // Get your User object from principal
            String jwt = jwtService.generateToken(user); // Pass User object to generate token
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) { // Catch specific AuthenticationException
            logger.warn("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) { // Catch any other unexpected exceptions
            logger.error("An unexpected error occurred during login for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "An internal server error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Authenticates an admin user and returns a JWT token.
     * Checks if the authenticated user has the 'ADMIN' role (from User.roles list after normalization).
     *
     * @param loginRequest The login credentials
     * @return ResponseEntity with JWT token or error
     */
    @PostMapping("/adminlogin")
    public ResponseEntity<Map<String, String>> adminLogin(@Valid @RequestBody LoginRequest loginRequest) { // Add @Valid
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            User user = (User) authentication.getPrincipal(); // Get your User object from principal

            // Check if the authenticated user has the "ROLE_ADMIN" authority (after normalization)
            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            if (user == null || !isAdmin) { // Check if user object is null or if they are not an Admin
                logger.warn("Admin login denied for user {}: Not an ADMIN.", loginRequest.getUsername());
                Map<String, String> error = new HashMap<>();
                error.put("message", "Admin access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            String jwt = jwtService.generateToken(user); // Pass User object to generate token
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            logger.warn("Admin authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid admin credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during admin login for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "An internal server error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Registers a new user.
     *
     * @param registerRequest The user registration details
     * @return ResponseEntity with JWT token and 201 status
     */
    @PostMapping("/register")
    @PreAuthorize("isAnonymous()") // Only allow unauthenticated users to register
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest registerRequest) { // Add @Valid
        try {
            // Create user object from DTO
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword()); // Raw password

            // Pass roles from RegisterRequest (now that DTO has it) to User entity
            // UserService.createUser will handle normalization and validation of these roles.
            user.setRoles(registerRequest.getRoles());

            User savedUser = userService.createUser(user); // This method assigns default USER role if roles are empty or invalid

            String jwt = jwtService.generateToken(savedUser); // Generate token for the newly registered user
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DuplicateEntryException e) { // Catch specific duplicate exceptions
            logger.warn("Registration failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) { // Catch any other unexpected exceptions
            logger.error("An unexpected error occurred during registration: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}