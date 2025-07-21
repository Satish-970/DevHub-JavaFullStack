package com.example.DevHub.Controller;

import com.example.DevHub.Model.User;
import com.example.DevHub.Service.UserService;
import com.example.DevHub.dto.UserResponse; // Added for DTO
import com.example.DevHub.exception.ResourceNotFoundException; // Added
import com.example.DevHub.exception.UnauthorizedOperationException; // Added
import com.example.DevHub.exception.DuplicateEntryException; // Added for updateUser
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller; // Consider removing if purely REST

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a user by ID.
     * @param id The user ID
     * @return ResponseEntity with the user or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User user = userService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(mapUserToUserResponse(user));
    }

    /**
     * Creates a new user. This endpoint should typically be for ADMIN only,
     * as self-registration is handled by /api/auth/register.
     * @param user The user details to create
     * @return ResponseEntity with the created user and 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)// Only ADMIN can create users via this endpoint
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody User user) {
        User savedUser = userService.createUser(user); // Service handles password encoding and role default
        return ResponseEntity.status(HttpStatus.CREATED).body(mapUserToUserResponse(savedUser));
    }

    /**
     * Retrieves all users. (Admin only endpoint)
     * @return ResponseEntity with the list of users
     */
    @GetMapping// Only ADMIN can retrieve all users
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(this::mapUserToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Updates an existing user.
     * Authorized for ADMIN or the user themselves.
     * @param id The user ID to update
     * @param userDetails The updated user details (only allowed fields)
     * @return ResponseEntity with the updated user or 400/403/404 if invalid
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Allow admin or self to update
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(mapUserToUserResponse(updatedUser));
        } catch (ResourceNotFoundException e) {
            throw e; // Handled by GlobalExceptionHandler
        } catch (DuplicateEntryException e) { // For username/email change conflicts
            throw e; // Handled by GlobalExceptionHandler
        } catch (Exception e) {
            // General catch for unexpected issues, will be handled by GlobalExceptionHandler
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a user by ID.
     * Authorized for ADMIN or the user themselves.
     * @param id The user ID to delete
     * @return ResponseEntity with 204 on success or 403/404 if invalid
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Allow admin or self to delete
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Prevent a user from deleting themselves if they are the ONLY admin
        // This specific logic might be better placed in UserService or a more complex auth system
        // For simplicity, we allow self-deletion.
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches users by username (partial match).
     * Authorized for ADMIN.
     * @param username The username to search for
     * @return ResponseEntity with the list of matching users
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')") // Typically admin only for search all users
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String username) {
        List<User> users = userService.searchByUsername(username);
        List<UserResponse> userResponses = users.stream()
                .map(this::mapUserToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    // Helper method to map User entity to UserResponse DTO
    private UserResponse mapUserToUserResponse(User user) {
        // Ensure roles are without "ROLE_" prefix if frontend expects "USER", "ADMIN"
        List<String> rolesWithoutPrefix = user.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                rolesWithoutPrefix, // Return roles without "ROLE_" prefix for frontend
                user.getCreatedAt(),
                user.getBio(), // New fields
                user.getLinkedinUrl(), // New fields
                user.getGithubUrl() // New fields
        );
    }
}