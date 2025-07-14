package com.example.DevHub.Controller;

import com.example.DevHub.Model.User;
import com.example.DevHub.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id") // Only ADMIN or the same user can access
    public ResponseEntity<User> getById(@PathVariable long id){
        User user = userService.getbyId(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Optional: Only ADMINs can create users manually
    public ResponseEntity<String> saveUser(@RequestBody User user){
        userService.saveUser(user);
        return ResponseEntity.ok("User created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<String> updateUser(@PathVariable long id, @RequestBody User user){
        userService.UpdateUser(user);
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable long id){
        userService.DeleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
