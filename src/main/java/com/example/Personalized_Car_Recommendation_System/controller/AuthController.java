package com.example.Personalized_Car_Recommendation_System.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 用户注册接口
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String confirmPassword = userData.get("confirmPassword");
        String role = userData.getOrDefault("role", "user");
        // 校验密码和确认密码是否匹配
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("密码和确认密码不匹配");
        }

        // 创建 User 对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        try {
            authService.register(user);
            return ResponseEntity.ok("注册成功，请登录");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 用户登录接口：传入 username 与 password，返回 JWT token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        System.out.println("aaaaa");
        String username = loginData.get("username");
        String password = loginData.get("password");
        String token = authService.login(username, password);
        System.out.println("aaaaa"+token);
        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
    }

    @PostMapping("/adminlogin")
    public ResponseEntity<?> adminlogin(
            @RequestBody Map<String, String> adminloginData) {
        String adminname = adminloginData.get("adminname");
        String password = adminloginData.get("password");
        String token = authService.adminlogin(adminname, password);
        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("管理员名或密码错误");
        }
    }
}
