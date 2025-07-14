package com.example.DevHub.Service;

import com.example.DevHub.Model.Project;
import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Creates a new project for the authenticated user.
     * @param project The project details to save
     * @return The saved project
     * @throws IllegalStateException if the authenticated user is not found
     */
    public Project createProject(Project project) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Authenticated user not found");
        }
        project.setCreatedBy(currentUser);
        return projectRepository.save(project);
    }

    /**
     * Retrieves all projects.
     * @return List of all projects
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Retrieves a project by ID.
     * @param id The project ID
     * @return Optional containing the project, or empty if not found
     */
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * Updates an existing project.
     * @param id The project ID to update
     * @param projectDetails The updated project details
     * @return The updated project
     * @throws IllegalStateException if the project is not found or the user is not authorized
     */
    public Project updateProject(Long id, Project projectDetails) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Project not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!existingProject.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to update this project");
        }

        existingProject.setTitle(projectDetails.getTitle());
        existingProject.setDescription(projectDetails.getDescription());
        existingProject.setUrl(projectDetails.getUrl());
        existingProject.setTechStack(projectDetails.getTechStack());
        return projectRepository.save(existingProject);
    }

    /**
     * Deletes a project by ID.
     * @param id The project ID to delete
     * @throws IllegalStateException if the project is not found or the user is not authorized
     */
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Project not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("User not authorized to delete this project");
        }

        projectRepository.delete(project);
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