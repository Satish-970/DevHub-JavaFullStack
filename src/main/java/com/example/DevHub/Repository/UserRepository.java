package com.example.DevHub.Repository;

import com.example.DevHub.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
