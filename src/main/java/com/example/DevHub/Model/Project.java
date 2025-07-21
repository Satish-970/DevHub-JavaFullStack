package com.example.DevHub.Model;

import com.fasterxml.jackson.annotation.JsonIgnore; // Add this import
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString; // Add this import
import org.hibernate.annotations.CreationTimestamp; // Added for createdAt
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"createdBy"}) // Exclude createdBy to prevent recursion
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @URL(message = "Invalid URL format")
    @Column(nullable = true)
    private String url; // For GitHub link

    @URL(message = "Invalid Demo URL format") // Added validation
    @Column(nullable = true)
    private String demoUrl; // Added for live demo link

    @NotBlank(message = "Tech stack is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String techStack;

    @CreationTimestamp // Added for creation timestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Prevent recursion and expose via DTOs in controllers
    private User createdBy;
}