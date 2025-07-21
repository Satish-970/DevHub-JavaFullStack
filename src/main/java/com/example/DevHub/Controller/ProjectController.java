package com.example.DevHub.Controller;
import com.example.DevHub.Model.Project;
import com.example.DevHub.Service.ProjectService;
import com.example.DevHub.exception.AuthenticationRequiredException; // Add imports for custom exceptions
import com.example.DevHub.exception.ResourceNotFoundException;
import com.example.DevHub.exception.UnauthorizedOperationException;
import jakarta.validation.Valid; // Assuming Project also has @Valid annotations for DTOs
import org.springframework.http.HttpStatus; // For ResponseEntity statuses
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role/owner checks
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // For getById

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    // A user creates their own project, so it's authorized if they're authenticated.
    // Service layer will assign 'createdBy' from current user.

    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(project));
        } catch (AuthenticationRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            // Catch other potential exceptions during creation if service throws them
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to get ALL projects (might be restricted to ADMIN or for specific searches)
    // If you only want to show *my* projects by default, remove this or make it admin-only.
    @GetMapping
   // For basic access, but filtering needs more specific endpoint
    public ResponseEntity<List<Project>> getAllProjects(@RequestParam(required = false) Long creatorId) {
        // This allows fetching all projects or filtering by creatorId if provided
        // The frontend will call /api/projects?creatorId={currentUserId}
        if (creatorId != null) {
            return ResponseEntity.ok(projectService.getProjectsByCreator(creatorId));
        }
        // If no creatorId, return all projects (maybe admin-only or public)
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    // Any authenticated user can view a specific project
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    // Only the creator or an ADMIN can update
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody Project projectDetails) {
        try {
            return ResponseEntity.ok(projectService.updateProject(id, projectDetails));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AuthenticationRequiredException e) { // Should be caught by AuthenticationEntryPoint but defensive
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{id}")
    // Only the creator or an ADMIN can delete
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build(); // 204 No Content for successful delete
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AuthenticationRequiredException e) { // Defensive
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}