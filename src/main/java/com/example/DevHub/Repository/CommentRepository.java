package com.example.DevHub.Repository;

import com.example.DevHub.Model.Comment;
import com.example.DevHub.Model.BlogPost;
import com.example.DevHub.Model.Project;
import com.example.DevHub.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Finds comments where comment.user.id = userId
    List<Comment> findByUserId(Long userId); // Correct for fetching comments by user ID

    // --- FIX FOR SERVICE ERROR ---
    // Use _Id convention to query by the ID of the associated entity
    List<Comment> findByBlogPost_Id(Long blogPostId); // Finds comments where comment.blogPost.id = blogPostId
    List<Comment> findByProject_Id(Long projectId);   // Finds comments where comment.project.id = projectId
    // --- END FIX ---
}