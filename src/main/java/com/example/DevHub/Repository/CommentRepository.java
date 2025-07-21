package com.example.DevHub.Repository;

import com.example.DevHub.Model.Comment;
import com.example.DevHub.Model.BlogPost; // Added import
import com.example.DevHub.Model.Project; // Added import
import com.example.DevHub.Model.User; // Added import
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBlogPost(BlogPost blogPost);
    List<Comment> findByProject(Project project);
    List<Comment> findByUser(User user);
    // You can add more methods as needed, e.g., find by user and blogpost
}