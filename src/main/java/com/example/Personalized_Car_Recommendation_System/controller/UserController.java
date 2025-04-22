package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.repository.UserRepository;
import com.example.Personalized_Car_Recommendation_System.service.UserService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import com.example.Personalized_Car_Recommendation_System.util.ValidationUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {



    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId,
                                        @RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String newPassword = (String) request.get("newPassword");
            Integer age = null;
            Object ageObj = request.get("age");
            if (ageObj != null) {
                if (ageObj instanceof Integer) {
                    age = (Integer) ageObj;
                } else if (ageObj instanceof String) {
                    age = ValidationUtils.parseAge((String) ageObj);
                }
            }
            String region = (String) request.get("region");

            // 数据验证
            ValidationUtils.validateUsername(username);

            User updatedUser = userService.updateUserInfo(userId, username, newPassword, age, region, passwordEncoder);
            if (updatedUser != null) {
                return new ResponseEntity<>(updatedUser, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("用户不存在", HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            logger.error("更新用户信息时参数错误: ", e);
            return new ResponseEntity<>("更新用户信息时参数错误，请检查输入", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("更新用户信息时出现异常: ", e);
            return new ResponseEntity<>("更新用户信息失败，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userService.addUser(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword) {
        return userService.searchUsers(keyword);
    }


}