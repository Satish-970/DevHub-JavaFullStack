package com.example.DevHub.Controller;

import com.example.DevHub.Model.User;
import com.example.DevHub.Service.JwtService;
import com.example.DevHub.Service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = {"Authorization", "Content-Type"},
        methods = {RequestMethod.GET, RequestMethod.POST}
)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
     * Authenticates a user and returns a JWT token with roles.
     *
     * @param loginRequest The login credentials
     * @return ResponseEntity with JWT token and roles or error
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Attempting login for username: " + loginRequest.getUsername()); // Debug log
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            String jwt = jwtService.generateToken(loginRequest.getUsername());
            User user = userService.findByUsername(loginRequest.getUsername()); // Throws UsernameNotFoundException if null
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("roles", user.getRoles() != null ? user.getRoles() : Arrays.asList("ROLE_USER")); // Fallback
            System.out.println("Login successful, token: " + jwt); // Debug log
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage()); // Debug log
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Registers a new user and returns a JWT token with roles.
     *
     * @param registerRequest The registration details
     * @return ResponseEntity with JWT token and roles or error message
     */
    @PostMapping("/register")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userService.findByUsername(registerRequest.getUsername()) != null) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Username already taken");
            return ResponseEntity.badRequest().body(error);
        }
        if (userService.findByEmail(registerRequest.getEmail())) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Email already registered");
            return ResponseEntity.badRequest().body(error);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setRoles(registerRequest.getRoles().isEmpty() ? Arrays.asList("ROLE_USER") : registerRequest.getRoles());
        user.setPassword(registerRequest.getPassword()); // Will be encoded in UserService
        try {
            user = userService.createUser(user);
            String jwt = jwtService.generateToken(user.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("roles", user.getRoles());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage()); // Debug log
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Handles successful OAuth2 login, generates JWT, and redirects to front-end index page.
     *
     * @param oAuth2User The authenticated GitHub user
     * @param response   The HTTP response for redirection
     * @throws IOException If redirection fails
     */
    @GetMapping("/oauth2/success")
    public void oauth2Success(@AuthenticationPrincipal OAuth2User oAuth2User, HttpServletResponse response) throws IOException {
        String username = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");

        System.out.println("OAuth2 success for username: " + username); // Debug log
        User user = userService.findByUsername(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setEmail(email != null ? email : username + "@github.com");
            user.setRoles(Arrays.asList("ROLE_USER"));
            user.setPassword(""); // No password for OAuth2 users
            user = userService.createUser(user);
            System.out.println("Created new user: " + username); // Debug log
        }

        String jwt = jwtService.generateToken(username);
        String redirectUrl = "http://localhost:3000/index.html?token=" + jwt; // Redirect to index.html
        System.out.println("Redirecting to: " + redirectUrl); // Debug log
        response.sendRedirect(redirectUrl);
    }
}

class LoginRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private List<String> roles = new ArrayList<>();

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}