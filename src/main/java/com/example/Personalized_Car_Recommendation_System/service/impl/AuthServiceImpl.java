package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.entity.Admin;
import com.example.Personalized_Car_Recommendation_System.repository.AdminRepository;
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
    @Autowired
    private AdminRepository adminRepository;

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
    public String login(String username, String password) { // 移除 userid 参数
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 使用数据库中的用户 ID 生成 Token
            return JwtUtil.generateToken(Long.valueOf(user.getId()));
        }
        return null;
    }


    @Override
    public String adminlogin(String adminname, String password) {
        Admin admin = adminRepository.findByAdminname(adminname);
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
            // 传递 admin_id 生成 JWT Token
            return JwtUtil.generateToken(Long.valueOf(admin.getId()));
        }
        return null;
    }
}