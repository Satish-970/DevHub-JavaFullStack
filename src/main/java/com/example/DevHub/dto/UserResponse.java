package com.example.DevHub.dto;

import java.time.LocalDateTime;
import java.util.List;

// This DTO is used to send user data to the frontend, excluding sensitive information like passwords.
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private LocalDateTime createdAt;
    private String bio; // Added
    private String linkedinUrl; // Added
    private String githubUrl; // Added

    // Constructor to map from User entity
    public UserResponse(Long id, String username, String email, List<String> roles, LocalDateTime createdAt, String bio, String linkedinUrl, String githubUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
        this.bio = bio;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getBio() {
        return bio;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }
}