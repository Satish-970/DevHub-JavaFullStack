package com.example.DevHub.Controller;

import com.example.DevHub.Model.User;
import com.example.DevHub.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService){
        this.userService=userService;
    }

    @GetMapping("/{id}")
    public User getbyId(@PathVariable  long id){
        return userService.getbyId(id);
    }
    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }
    @PostMapping
    public void saveUser(User user){
        userService.saveUser(user);
    }
    @PutMapping("/{id}")
    public  void UpdateUser(@PathVariable long id, @RequestBody User user){
        userService.UpdateUser(user);
    }
    @DeleteMapping("/{id}")
    public  void DeleteUser(@PathVariable long id){
        userService.DeleteUser(id);
    }
}
