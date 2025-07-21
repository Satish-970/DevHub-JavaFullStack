package com.example.DevHub.Service;

import com.example.DevHub.Model.Comment;
import com.example.DevHub.Model.BlogPost;
import com.example.DevHub.Model.Project;
import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.CommentRepository;
import com.example.DevHub.Repository.BlogPostRepository; // Added for creating blog comments
import com.example.DevHub.Repository.ProjectRepository; // Added for creating project comments
import com.example.DevHub.exception.AuthenticationRequiredException;
import com.example.DevHub.exception.ResourceNotFoundException;
import com.example.DevHub.exception.UnauthorizedOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Added for timestamp
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository; // Injected
    private final ProjectRepository projectRepository;   // Injected
    private final UserService userService;               // Injected

    @Autowired
    public CommentService(CommentRepository commentRepository, BlogPostRepository blogPostRepository, ProjectRepository projectRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.blogPostRepository = blogPostRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    /**
     * Creates a new comment associated with a blog post.
     * @param comment The comment details
     * @param blogPostId The ID of the blog post
     * @return The saved comment
     * @throws AuthenticationRequiredException if no user is authenticated
     * @throws ResourceNotFoundException if the blog post is not found
     */
    public Comment createBlogComment(Comment comment, Long blogPostId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to comment.");
        }

        BlogPost blogPost = blogPostRepository.findById(blogPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + blogPostId));

        comment.setUser(currentUser);
        comment.setBlogPost(blogPost);
        comment.setCommentedAt(LocalDateTime.now()); // Ensure timestamp is set
        return commentRepository.save(comment);
    }

    /**
     * Creates a new comment associated with a project.
     * @param comment The comment details
     * @param projectId The ID of the project
     * @return The saved comment
     * @throws AuthenticationRequiredException if no user is authenticated
     * @throws ResourceNotFoundException if the project is not found
     */
    public Comment createProjectComment(Comment comment, Long projectId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to comment.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        comment.setUser(currentUser);
        comment.setProject(project);
        comment.setCommentedAt(LocalDateTime.now()); // Ensure timestamp is set
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
     * @throws ResourceNotFoundException if the comment is not found
     * @throws UnauthorizedOperationException if the user is not authorized
     * @throws AuthenticationRequiredException if no user is authenticated
     */
    public Comment updateComment(Long id, Comment commentDetails) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to update a comment.");
        }

        // Only the original author or an ADMIN can update a comment
        if (!existingComment.getUser().getId().equals(currentUser.getId()) && !currentUser.getRoles().contains("ADMIN")) {
            throw new UnauthorizedOperationException("User not authorized to update this comment.");
        }

        existingComment.setContent(commentDetails.getContent());
        // UpdatedAt will be handled automatically if @UpdateTimestamp was added, otherwise manual
        return commentRepository.save(existingComment);
    }

    /**
     * Deletes a comment by ID.
     * @param id The comment ID to delete
     * @throws ResourceNotFoundException if the comment is not found
     * @throws UnauthorizedOperationException if the user is not authorized
     * @throws AuthenticationRequiredException if no user is authenticated
     */
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to delete a comment.");
        }

        // Only the original author or an ADMIN can delete a comment
        if (!comment.getUser().getId().equals(currentUser.getId()) && !currentUser.getRoles().contains("ADMIN")) {
            throw new UnauthorizedOperationException("User not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
    }

    /**
     * Retrieves comments for a specific blog post.
     * @param blogPostId The ID of the blog post
     * @return List of comments for the blog post
     * @throws ResourceNotFoundException if blog post not found
     */
    public List<Comment> getCommentsByBlogPostId(Long blogPostId) {
        BlogPost blogPost = blogPostRepository.findById(blogPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + blogPostId));
        return commentRepository.findByBlogPost(blogPost);
    }

    /**
     * Retrieves comments for a specific project.
     * @param projectId The ID of the project
     * @return List of comments for the project
     * @throws ResourceNotFoundException if project not found
     */
    public List<Comment> getCommentsByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return commentRepository.findByProject(project);
    }

    /**
     * Retrieves comments made by a specific user.
     * @param userId The ID of the user
     * @return List of comments by the user
     * @throws ResourceNotFoundException if user not found
     */
    public List<Comment> getCommentsByUserId(Long userId) {
        User user = userService.getById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return commentRepository.findByUser(user);
    }

    /**
     * Helper to get current authenticated user. Assumes principal is User object.
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null; // Should be handled by @PreAuthorize or AuthenticationRequiredException
    }
}