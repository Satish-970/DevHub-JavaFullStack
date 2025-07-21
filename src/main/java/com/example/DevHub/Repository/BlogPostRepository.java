package com.example.DevHub.Repository;

import com.example.DevHub.Model.BlogPost;
import com.example.DevHub.Model.User; // Added import
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    // Custom query to find blog posts by a specific author
    List<BlogPost> findByAuthor(User author);
    // You can add more specific queries if needed, e.g., search by title/tags
    List<BlogPost> findByTitleContainingIgnoreCase(String title);
    List<BlogPost> findByTagsContainingIgnoreCase(String tag);
    List<BlogPost> findByAuthorId(Long authorId);
}