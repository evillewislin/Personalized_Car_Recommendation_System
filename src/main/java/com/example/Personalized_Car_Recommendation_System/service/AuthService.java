package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.entity.User;

public interface AuthService {
    User register(User user);
    String login(String username, String password);

    String adminlogin(String adminname, String password);
}