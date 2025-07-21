package com.example.DevHub.Model;

import com.fasterxml.jackson.annotation.JsonIgnore; // Add this import
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Added for content validation
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "blogPost", "project"}) // Exclude related entities to prevent recursion
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Comment content is required") // Added validation
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime commentedAt;

    @ManyToOne(fetch = FetchType.LAZY) // Made optional=true by default, nullable = true
    @JoinColumn(name = "user_id", nullable = false) // User is always required
    @JsonIgnore // Prevent recursion
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id") // Made nullable
    @JsonIgnore // Prevent recursion
    private BlogPost blogPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id") // NEW: For comments on projects, made nullable
    @JsonIgnore // Prevent recursion
    private Project project; // NEW: Added Project relationship
}