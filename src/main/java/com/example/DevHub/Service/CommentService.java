package com.example.DevHub.Service;

import com.example.DevHub.Model.Comment;
import com.example.DevHub.Model.BlogPost;
import com.example.DevHub.Model.Project;
import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.CommentRepository;
import com.example.DevHub.Repository.BlogPostRepository;
import com.example.DevHub.Repository.ProjectRepository;
import com.example.DevHub.exception.AuthenticationRequiredException;
import com.example.DevHub.exception.ResourceNotFoundException;
import com.example.DevHub.exception.UnauthorizedOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Autowired
    public CommentService(CommentRepository commentRepository, BlogPostRepository blogPostRepository, ProjectRepository projectRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.blogPostRepository = blogPostRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment createComment(Comment comment, Long parentId, String parentType) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to create a comment.");
        }
        comment.setUser(currentUser);
        comment.setCommentedAt(LocalDateTime.now());

        if ("blog".equalsIgnoreCase(parentType)) {
            BlogPost blogPost = blogPostRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with ID: " + parentId));
            comment.setBlogPost(blogPost);
            comment.setProject(null);
        } else if ("project".equalsIgnoreCase(parentType)) {
            Project project = projectRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + parentId));
            comment.setProject(project);
            comment.setBlogPost(null);
        } else {
            throw new IllegalArgumentException("Invalid parent type for comment: " + parentType);
        }

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to view their comments.");
        }
        return commentRepository.findByUserId(currentUser.getId());
    }

    // --- FIX FOR SERVICE ERROR (using _Id) ---
    public List<Comment> getCommentsByBlogPostId(Long blogPostId) {
        return commentRepository.findByBlogPost_Id(blogPostId); // Uses the new repository method
    }

    public List<Comment> getCommentsByProjectId(Long projectId) {
        return commentRepository.findByProject_Id(projectId); // Uses the new repository method
    }
    // --- END FIX ---

    public Comment updateComment(Long id, Comment commentDetails) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to update a comment.");
        }

        boolean isAuthor = existingComment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedOperationException("User not authorized to update this comment.");
        }

        existingComment.setContent(commentDetails.getContent());
        return commentRepository.save(existingComment);
    }

    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to delete a comment.");
        }

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedOperationException("User not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null ||
                (authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return null;
        }

        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
            return userService.findByUsername(username);
        }
        return null;
    }
}