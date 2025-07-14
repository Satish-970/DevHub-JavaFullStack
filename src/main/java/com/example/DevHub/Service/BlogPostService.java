package com.example.DevHub.Service;

import com.example.DevHub.Model.BlogPost;
import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;

    @Autowired
    public BlogPostService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    /**
     * Creates a new blog post for the authenticated user.
     * @param blogPost The blog post details to save
     * @return The saved blog post
     * @throws IllegalStateException if the authenticated user is not found
     */
    public BlogPost createBlogPost(BlogPost blogPost) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Authenticated user not found");
        }
        blogPost.setAuthor(currentUser);
        return blogPostRepository.save(blogPost);
    }

    /**
     * Retrieves all blog posts.
     * @return List of all blog posts
     */
    public List<BlogPost> getAllBlogPosts() {
        return blogPostRepository.findAll();
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
     * @throws IllegalStateException if the blog post is not found or the user is not authorized
     */
    public BlogPost updateBlogPost(Long id, BlogPost blogPostDetails) {
        BlogPost existingBlogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!existingBlogPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to update this blog post");
        }

        existingBlogPost.setTitle(blogPostDetails.getTitle());
        existingBlogPost.setContent(blogPostDetails.getContent());
        existingBlogPost.setTags(blogPostDetails.getTags());
        return blogPostRepository.save(existingBlogPost);
    }

    /**
     * Deletes a blog post by ID.
     * @param id The blog post ID to delete
     * @throws IllegalStateException if the blog post is not found or the user is not authorized
     */
    public void deleteBlogPost(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!blogPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to delete this blog post");
        }

        blogPostRepository.delete(blogPost);
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