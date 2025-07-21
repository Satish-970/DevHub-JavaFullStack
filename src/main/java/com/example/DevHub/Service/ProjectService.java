package com.example.DevHub.Service;

import com.example.DevHub.Model.Project;
import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.ProjectRepository;
import com.example.DevHub.exception.AuthenticationRequiredException;
import com.example.DevHub.exception.ResourceNotFoundException;
import com.example.DevHub.exception.UnauthorizedOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication; // Added import for Authentication
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService; // Injected for getCurrentUser

    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    /**
     * Creates a new project for the authenticated user.
     * @param project The project details to save
     * @return The saved project
     * @throws AuthenticationRequiredException if the authenticated user is not found
     */
    public Project createProject(Project project) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to create a project.");
        }
        project.setCreatedBy(currentUser);
        // Set creation timestamp if not already handled by @CreationTimestamp in Project model
        // If Project model has @CreationTimestamp, this explicit setting might be redundant but harmless if null.
        if (project.getCreatedAt() == null) { // Assuming you added createdAt to Project model based on previous analysis
            project.setCreatedAt(LocalDateTime.now());
        }
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
     * @throws ResourceNotFoundException if the project is not found
     * @throws UnauthorizedOperationException if the user is not authorized
     * @throws AuthenticationRequiredException if no user is authenticated
     */
    public Project updateProject(Long id, Project projectDetails) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to update a project.");
        }

        // Check if current user is the creator or has ROLE_ADMIN
        // getAuthorities() will provide roles prefixed with "ROLE_" from CustomUserDetailsService
        boolean isCreator = existingProject.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isCreator && !isAdmin) {
            throw new UnauthorizedOperationException("User not authorized to update this project.");
        }

        // Update fields that can be changed
        existingProject.setTitle(projectDetails.getTitle());
        existingProject.setDescription(projectDetails.getDescription());
        existingProject.setUrl(projectDetails.getUrl());
        existingProject.setTechStack(projectDetails.getTechStack());
        existingProject.setDemoUrl(projectDetails.getDemoUrl()); // Ensure Project model has getDemoUrl()

        // @UpdateTimestamp in the Model will handle updatedAt automatically if present

        return projectRepository.save(existingProject);
    }

    /**
     * Deletes a project by ID.
     * @param id The project ID to delete
     * @throws ResourceNotFoundException if the project is not found
     * @throws UnauthorizedOperationException if the user is not authorized
     * @throws AuthenticationRequiredException if no user is authenticated
     */
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to delete a project.");
        }

        // Check if current user is the creator or has ROLE_ADMIN
        boolean isCreator = project.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isCreator && !isAdmin) {
            throw new UnauthorizedOperationException("User not authorized to delete this project.");
        }

        projectRepository.delete(project);
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return The authenticated User entity, or null if not found/authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null ||
                (authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return null; // No authenticated user or anonymous user
        }

        // IMPORTANT: Assuming CustomUserDetailsService returns your actual User entity (com.example.DevHub.Model.User)
        // This is the recommended approach for seamless principal access.
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        // FALLBACK: If CustomUserDetailsService returns Spring's default UserDetails (less ideal for this app).
        // This will make an extra database call to fetch the full User entity.
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
            return userService.findByUsername(username); // findByUsername returns User and throws UsernameNotFoundException
        }

        // If principal is of an unexpected type, which should ideally not happen in a well-configured app.
        return null; // Or throw an IllegalArgumentException for an unexpected principal type
    }
    // In ProjectService.java
    public List<Project> getProjectsByCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationRequiredException("User must be authenticated to view their projects.");
        }
        return projectRepository.findByCreatedById(currentUser.getId()); // You'll need this method in ProjectRepository
    }
    // If you need to view *any* user's projects
    public List<Project> getProjectsByCreator(Long creatorId) {
        return projectRepository.findByCreatedById(creatorId);
    }
}