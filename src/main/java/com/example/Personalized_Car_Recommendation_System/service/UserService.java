package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Integer id);
    User addUser(User user);
    User updateUser(User user);
    void deleteUser(Integer id);
    List<User> searchUsers(String keyword);
    User updateUsername(Integer userId, String username);
    User updateUserInfo(Integer userId, String username, String newPassword, PasswordEncoder passwordEncoder);
}