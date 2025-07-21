package com.example.DevHub.Service;

import com.example.DevHub.Model.User; // Added import for User model
import com.example.DevHub.Repository.UserRepository; // Added import for UserRepository
import com.example.DevHub.exception.ResourceNotFoundException; // Added for exception
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException; // Added
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException; // Added
import io.jsonwebtoken.SignatureException; // Added
import io.jsonwebtoken.UnsupportedJwtException; // Added
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger; // Added
import org.slf4j.LoggerFactory; // Added
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List; // Added for roles claim
import java.util.stream.Collectors;

@Component
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class); // Added logger

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private SecretKey secretKey;
    // UserRepository is generally not needed here if User object is passed directly
    // private final UserRepository userRepository;

    // Removed UserRepository from constructor, pass User object directly to generateToken
    public JwtService(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expirationMs}") long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        // this.userRepository = userRepository; // Removed
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Modified to accept User object directly
    public String generateToken(User user) {
        // User object already contains all necessary data, no need for lookup here
        // user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found: " + username));

        // Ensure roles are prefixed with ROLE_ in the JWT claim
        List<String> rolesWithPrefix = user.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId()) // Add user ID to claims for easier access on frontend/PreAuthorize
                .claim("roles", rolesWithPrefix) // Use prefixed roles
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    // Improved error logging for validateToken
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) { // Catch any other unexpected exception
            logger.error("An unexpected error occurred during JWT validation: {}", e.getMessage());
        }
        return false;
    }
}