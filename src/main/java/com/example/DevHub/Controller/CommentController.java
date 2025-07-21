package com.example.DevHub.Controller;

import com.example.DevHub.Model.Comment;
import com.example.DevHub.Service.CommentService;
import com.example.DevHub.exception.AuthenticationRequiredException; // Import custom exceptions
import com.example.DevHub.exception.ResourceNotFoundException;
import com.example.DevHub.exception.UnauthorizedOperationException;
import jakarta.validation.Valid; // For @Valid annotation
import org.springframework.http.HttpStatus; // For ResponseEntity statuses
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For @PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Consolidated endpoint for creating comments on either blog or project
    // This MUST match the frontend's fetch(`${commentBaseUrl}/${parentType}/${parentId}`)
    @PostMapping("/{parentType}/{parentId}") // e.g., /api/comments/blog/1 or /api/comments/project/5
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Only authenticated users can comment
    public ResponseEntity<Comment> createComment(@PathVariable String parentType, @PathVariable Long parentId, @Valid @RequestBody Comment comment) {
        try {
            Comment savedComment = commentService.createComment(comment, parentId, parentType); // Call consolidated service method
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
        } catch (ResourceNotFoundException e) {
            // Catches if blog/project not found
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            // Catches if parentType is invalid (not "blog" or "project")
            return ResponseEntity.badRequest().body(null); // Or return a specific error message here
        } catch (AuthenticationRequiredException e) { // Defensive check
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Keep existing GET, PUT, DELETE methods, ensuring they use correct exceptions and PreAuthorize

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Comment>> getAllComments(@RequestParam(required = false) Long userId,
                                                        @RequestParam(required = false) Long blogPostId,
                                                        @RequestParam(required = false) Long projectId) {
        if (userId != null) {
            return ResponseEntity.ok(commentService.getCommentsByCurrentUser());
        }
        if (blogPostId != null) {
            return ResponseEntity.ok(commentService.getCommentsByBlogPostId(blogPostId));
        }
        if (projectId != null) {
            return ResponseEntity.ok(commentService.getCommentsByProjectId(projectId));
        }
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @commentService.getCommentById(#id).orElse(null)?.user?.id == principal.id")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @Valid @RequestBody Comment commentDetails) {
        try {
            return ResponseEntity.ok(commentService.updateComment(id, commentDetails));
        } catch (IllegalStateException e) { // Still using IllegalStateException, should use custom ones
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Catch generic for now, refine with custom exceptions
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @commentService.getCommentById(#id).orElse(null)?.user?.id == principal.id")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) { // Still using IllegalStateException, should use custom ones
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}