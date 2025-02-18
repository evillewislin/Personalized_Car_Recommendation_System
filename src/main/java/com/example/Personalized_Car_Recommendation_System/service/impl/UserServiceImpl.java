package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.repository.UserRepository;
import com.example.Personalized_Car_Recommendation_System.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findByUserId(userId);
    }

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> user.getUsername().contains(keyword))
                .collect(Collectors.toList());
    }
    @Override
    public User updateUsername(Integer userId, String username) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setUsername(username);
            return userRepository.save(user);
        }
        return null;
    }
}