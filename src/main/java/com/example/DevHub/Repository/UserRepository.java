package com.example.DevHub.Repository;

import com.example.DevHub.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    Optional<User> findByEmail(String email); // Added for retrieval by email

    boolean existsByUsername(String username); // Added explicit existence check
    boolean existsByEmail(String email); // Corrected to return boolean
}