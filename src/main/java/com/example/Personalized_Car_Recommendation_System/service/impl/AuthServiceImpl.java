package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.repository.UserRepository;
import com.example.Personalized_Car_Recommendation_System.service.AuthService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 加密密码后存储
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return JwtUtil.generateToken(username);
        }
        return null;
    }
}
