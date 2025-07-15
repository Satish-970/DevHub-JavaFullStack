package com.example.DevHub.Service;

import com.example.DevHub.Model.Comment;
import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Creates a new comment for the authenticated user on a specific blog post.
     * @param comment The comment details to save
     * @param blogPostId The ID of the blog post
     * @return The saved comment
     * @throws IllegalStateException if the authenticated user or blog post is not found
     */
    public Comment createComment(Comment comment, Long blogPostId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Authenticated user not found");
        }
        // Assuming BlogPostService or a method to load blog post by ID
        // For now, this is a placeholder; you'll need to implement BlogPostService
        // BlogPost blogPost = blogPostService.findById(blogPostId)
        //     .orElseThrow(() -> new IllegalStateException("Blog post not found with id: " + blogPostId));
        comment.setUser(currentUser);
        // comment.setBlogPost(blogPost); // Uncomment and adjust when BlogPostService is ready
        return commentRepository.save(comment);
    }

    /**
     * Retrieves all comments.
     * @return List of all comments
     */
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    /**
     * Retrieves a comment by ID.
     * @param id The comment ID
     * @return Optional containing the comment, or empty if not found
     */
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    /**
     * Updates an existing comment.
     * @param id The comment ID to update
     * @param commentDetails The updated comment details
     * @return The updated comment
     * @throws IllegalStateException if the comment is not found or the user is not authorized
     */
    public Comment updateComment(Long id, Comment commentDetails) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Comment not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!existingComment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to update this comment");
        }

        existingComment.setContent(commentDetails.getContent());
        return commentRepository.save(existingComment);
    }

    /**
     * Deletes a comment by ID.
     * @param id The comment ID to delete
     * @throws IllegalStateException if the comment is not found or the user is not authorized
     */
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Comment not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return The authenticated User, or null if not found
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // Assuming UserService or a similar method to load user by username
            return userService.findByUsername(username); // Requires UserService injection
        }
        return null;
    }

    // Inject UserService if needed for getCurrentUser
    @Autowired
    private UserService userService; // Add this field if not already present
}