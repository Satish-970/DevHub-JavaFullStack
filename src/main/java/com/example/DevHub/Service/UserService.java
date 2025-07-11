package com.example.DevHub.Service;

import com.example.DevHub.Model.User;
import com.example.DevHub.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }
    public void saveUser(User user){
        userRepository.save(user);
    }
    public List<User> getAllUsers(){
        return  userRepository.findAll();
    }
    public  User getbyId(long id){
        return userRepository.getById(id);
    }
    public void UpdateUser(User user){
        userRepository.save(user);
    }
    public  void DeleteUser(long id){
        userRepository.deleteById(id);
    }
}
