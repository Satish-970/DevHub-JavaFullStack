package com.example.DevHub.Security;

import com.example.DevHub.Service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
// Removed io.jsonwebtoken.security.Keys import (as secretKey is removed)
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // Still needed if you inject @Value, but we will remove it from constructor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey; // Keep this import for SecretKey type, but the field is removed
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService; // Correctly injected

    // REMOVED: private final SecretKey secretKey;
    // REMOVED: @Value("${jwt.secret}") String jwtSecret (from constructor)

    // Constructor now only takes services it depends on
    public JwtAuthenticationFilter(UserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        // REMOVED: this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Skip JWT validation for /api/auth/** endpoints (login, register) and static resources
        // Ensure /assets/ is correctly covered here
        if (requestURI.startsWith("/api/auth/") || requestURI.equals("/login") || requestURI.equals("/register")
                || requestURI.equals("/index.html") || requestURI.startsWith("/css/") || requestURI.startsWith("/js/") || requestURI.startsWith("/assets/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = getJwtFromRequest(request);

        if (jwt != null) {
            try {
                // --- Delegate ALL JWT validation and parsing to JwtService ---
                // JwtService.validateToken will throw specific exceptions if the token is invalid
                jwtService.validateToken(jwt);

                // If validateToken doesn't throw, token is valid, now get username
                String username = jwtService.getUsernameFromToken(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Successfully authenticated user: {}", username);
                    } else {
                        logger.warn("User not found in DB for username from token: {}", username);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User associated with token not found.");
                        return;
                    }
                }
            } catch (ExpiredJwtException e) {
                // Catch specific JWT exceptions re-thrown by JwtService
                logger.warn("Expired JWT token for user {}: {}", (e.getClaims() != null ? e.getClaims().getSubject() : "unknown"), e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired authentication token.");
                return;
            } catch (UnsupportedJwtException e) {
                logger.error("Unsupported JWT token: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported authentication token.");
                return;
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT token format: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication token format.");
                return;
            } catch (SignatureException e) {
                logger.error("Invalid JWT signature: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication token signature.");
                return;
            } catch (IllegalArgumentException e) {
                logger.error("JWT claims string is empty or malformed: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or malformed authentication token claims.");
                return;
            } catch (Exception e) { // Catch any other unexpected authentication errors
                logger.error("An unexpected error occurred during JWT authentication: {}", e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication token processing error.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}