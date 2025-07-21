package com.example.DevHub.Repository;

import com.example.DevHub.Model.Project;
import com.example.DevHub.Model.User; // Added import
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Custom query to find projects created by a specific user
    List<Project> findByCreatedBy(User createdBy);
    // You can add more specific queries if needed, e.g., search by title/tech stack
    List<Project> findByTitleContainingIgnoreCase(String title);
    List<Project> findByTechStackContainingIgnoreCase(String techStack);
    List<Project> findByCreatedById(Long createdById);
}