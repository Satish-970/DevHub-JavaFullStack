package com.example.DevHub.Service;

import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        List<String> validatedRoles = new ArrayList<>();
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            validatedRoles.add("ROLE_USER");
        } else {
            for (String role : user.getRoles()) {
                String normalized = role.toUpperCase();
                if (normalized.equals("ROLE_USER") || normalized.equals("ROLE_ADMIN")) {
                    validatedRoles.add(normalized);
                } else {
                    System.out.println("Ignored invalid role: " + role); // Debug log
                }
            }
            if (validatedRoles.isEmpty()) {
                validatedRoles.add("ROLE_USER");
            }
        }
        user.setRoles(validatedRoles);

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + id));
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public List<User> searchByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public boolean findByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}