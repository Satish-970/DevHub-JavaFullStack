package com.example.DevHub.Service;

import com.example.DevHub.Model.BlogPost;
import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.BlogPostRepository;
import com.example.DevHub.exception.AuthenticationRequiredException;
import com.example.DevHub.exception.ResourceNotFoundException;
import com.example.DevHub.exception.UnauthorizedOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final UserService userService; // Injected for getCurrentUser

    @Autowired
    public BlogPostService(BlogPostRepository blogPostRepository, UserService userService) {
        this.blogPostRepository = blogPostRepository;
        this.userService = userService;
    }

    /**
     * Creates a new blog post for the authenticated user.
     * @param blogPost The blog post details to save
     * @return The saved blog post
     * @throws AuthenticationRequiredException if the authenticated user is not found
     */
    public BlogPost createBlogPost(BlogPost blogPost) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to create a blog post.");
        }
        blogPost.setAuthor(currentUser);
        // Ensure createdAt is set by @CreationTimestamp in Model, or explicitly here if needed.
        if (blogPost.getCreatedAt() == null) {
            blogPost.setCreatedAt(LocalDateTime.now());
        }
        return blogPostRepository.save(blogPost);
    }

    /**
     * Retrieves all blog posts (can be filtered by authorId).
     * @return List of all blog posts
     */
    public List<BlogPost> getAllBlogPosts() {
        return blogPostRepository.findAll();
    }

    /**
     * Retrieves blog posts by a specific author ID.
     * @param authorId The ID of the author whose blogs to retrieve.
     * @return List of blog posts by the specified author.
     */
    public List<BlogPost> getBlogPostsByAuthor(Long authorId) {
        return blogPostRepository.findByAuthorId(authorId);
    }

    /**
     * Retrieves blog posts created by the currently authenticated user.
     * @return List of blog posts by the current user.
     * @throws AuthenticationRequiredException if no user is authenticated.
     */
    public List<BlogPost> getBlogPostsByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to view their blogs.");
        }
        return blogPostRepository.findByAuthorId(currentUser.getId());
    }

    /**
     * Retrieves a blog post by ID.
     * @param id The blog post ID
     * @return Optional containing the blog post, or empty if not found
     */
    public Optional<BlogPost> getBlogPostById(Long id) {
        return blogPostRepository.findById(id);
    }

    /**
     * Updates an existing blog post.
     * @param id The blog post ID to update
     * @param blogPostDetails The updated blog post details
     * @return The updated blog post
     * @throws ResourceNotFoundException if the blog post is not found
     * @throws UnauthorizedOperationException if the user is not authorized
     * @throws AuthenticationRequiredException if no user is authenticated
     */
    public BlogPost updateBlogPost(Long id, BlogPost blogPostDetails) {
        BlogPost existingBlogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to update a blog post.");
        }

        // Check if current user is the author or an ADMIN
        boolean isAuthor = existingBlogPost.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedOperationException("User not authorized to update this blog post.");
        }

        existingBlogPost.setTitle(blogPostDetails.getTitle());
        existingBlogPost.setContent(blogPostDetails.getContent());
        existingBlogPost.setTags(blogPostDetails.getTags());
        // @UpdateTimestamp in the Model will handle updatedAt automatically if present

        return blogPostRepository.save(existingBlogPost);
    }

    /**
     * Deletes a blog post by ID.
     * @param id The blog post ID to delete
     * @throws ResourceNotFoundException if the blog post is not found
     * @throws UnauthorizedOperationException if the user is not authorized
     * @throws AuthenticationRequiredException if no user is authenticated
     */
    public void deleteBlogPost(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to delete a blog post.");
        }

        // Check if current user is the author or an ADMIN
        boolean isAuthor = blogPost.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedOperationException("User not authorized to delete this blog post.");
        }

        blogPostRepository.delete(blogPost);
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return The authenticated User entity, or null if not found/authenticated
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null || (principal instanceof String && "anonymousUser".equals(principal))) {
            return null;
        }

        if (principal instanceof User) {
            return (User) principal;
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userService.findByUsername(username);
        }
        return null;
    }
}