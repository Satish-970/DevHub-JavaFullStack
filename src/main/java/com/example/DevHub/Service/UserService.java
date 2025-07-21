package com.example.DevHub.Service;

import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.UserRepository;
import com.example.DevHub.exception.DuplicateEntryException;
import com.example.DevHub.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        // Check for duplicate username
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateEntryException("Username '" + user.getUsername() + "' already taken.");
        }
        // Check for duplicate email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEntryException("Email '" + user.getEmail() + "' already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign default role(s) or validate provided roles
        // For self-registration, new users usually get a default role
        Set<String> validatedRoles = new HashSet<>(); // Use Set for roles for uniqueness
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            validatedRoles.add("USER"); // Default role for new users (not ROLE_USER, because normalization happens later in User.getAuthorities)
        } else {
            // If roles can be provided (e.g., by admin or special registration), validate them
            for (String role : user.getRoles()) {
                String normalized = role.toUpperCase();
                // Remove "ROLE_" prefix if present to normalize to just "USER" or "ADMIN" for storage
                if (normalized.startsWith("ROLE_")) {
                    normalized = normalized.substring(5);
                }
                if (normalized.equals("USER") || normalized.equals("ADMIN")) { // Only allow these specific roles
                    validatedRoles.add(normalized);
                } else {
                    System.out.println("Ignored invalid role: " + role); // Log for debugging
                }
            }
            if (validatedRoles.isEmpty()) {
                validatedRoles.add("USER"); // Fallback to default if all provided roles were invalid
            }
        }
        // Ensure User model uses List<String> for roles, so convert Set back to List
        user.setRoles(new ArrayList<>(validatedRoles));

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check for duplicate username during update (if username is being changed)
        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(userDetails.getUsername())) {
                throw new DuplicateEntryException("Username '" + userDetails.getUsername() + "' already taken.");
            }
            existingUser.setUsername(userDetails.getUsername());
        }

        // Check for duplicate email during update (if email is being changed)
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new DuplicateEntryException("Email '" + userDetails.getEmail() + "' already registered.");
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        // Only update password if a new one is provided and not empty
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // Update profile fields (assuming User model has these, and getters/setters work)
        if (userDetails.getBio() != null) {
            existingUser.setBio(userDetails.getBio());
        }
        if (userDetails.getLinkedinUrl() != null) {
            existingUser.setLinkedinUrl(userDetails.getLinkedinUrl());
        }
        if (userDetails.getGithubUrl() != null) {
            existingUser.setGithubUrl(userDetails.getGithubUrl());
        }


        // Roles update: This method is for general user updates. Roles should typically
        // be updated via a separate, admin-specific endpoint/method.
        // If roles ARE allowed to be updated here (e .g., by an admin for a user),
        // you'd need validation/normalization similar to createUser.

        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            Set<String> updatedRoles = new HashSet<>();
            for (String role : userDetails.getRoles()) {
                String normalized = role.toUpperCase();
                if (normalized.startsWith("ROLE_")) {
                    normalized = normalized.substring(5);
                }
                if (normalized.equals("USER") || normalized.equals("ADMIN")) {
                    updatedRoles.add(normalized);
                }
            }
            if (!updatedRoles.isEmpty()) {
                existingUser.setRoles(new ArrayList<>(updatedRoles));
            } else {

                // Decide what to do if invalid roles are provided: keep old roles, default to USER, or throw error
                // For now, if provided and invalid, they won't be added.
            }
        }


        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public List<User> searchByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    // Renamed from findByEmail to existsByEmail for clarity of boolean return
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Added to retrieve user by email if needed (distinct from existsByEmail)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}