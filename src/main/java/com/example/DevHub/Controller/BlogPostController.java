package com.example.DevHub.Controller;

import com.example.DevHub.Model.BlogPost;
import com.example.DevHub.Service.BlogPostService;
import com.example.DevHub.exception.AuthenticationRequiredException; // Add these imports
import com.example.DevHub.exception.ResourceNotFoundException;
import com.example.DevHub.exception.UnauthorizedOperationException;
import jakarta.validation.Valid; // Assuming BlogPost also has @Valid annotations
import org.springframework.http.HttpStatus; // For ResponseEntity statuses
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role/owner checks
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog-posts")
public class BlogPostController {

    private final BlogPostService blogPostService;

    public BlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @PostMapping
    public ResponseEntity<BlogPost> createBlogPost(@Valid @RequestBody BlogPost blogPost) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(blogPostService.createBlogPost(blogPost));
        } catch (AuthenticationRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
   // Any authenticated user can list/filter
    public ResponseEntity<List<BlogPost>> getAllBlogPosts(@RequestParam(required = false) Long authorId) {
        if (authorId != null) {
            // If authorId is provided, filter by author. The service will handle authorization check for this if needed.
            return ResponseEntity.ok(blogPostService.getBlogPostsByAuthor(authorId));
        }
        // If no authorId, return all blogs (e.g., for an admin view or general public view)
        // You might want to filter this further in the service if it's only for admins.
        return ResponseEntity.ok(blogPostService.getAllBlogPosts());
    }

    @GetMapping("/{id}")
     // Any authenticated user can view a specific blog
    public ResponseEntity<BlogPost> getBlogPostById(@PathVariable Long id) {
        return blogPostService.getBlogPostById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    // Only the author or an ADMIN can update
    public ResponseEntity<BlogPost> updateBlogPost(@PathVariable Long id, @Valid @RequestBody BlogPost blogPostDetails) {
        try {
            return ResponseEntity.ok(blogPostService.updateBlogPost(id, blogPostDetails));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AuthenticationRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{id}")
    // Only the author or an ADMIN can delete
    public ResponseEntity<Void> deleteBlogPost(@PathVariable Long id) {
        try {
            blogPostService.deleteBlogPost(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AuthenticationRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}