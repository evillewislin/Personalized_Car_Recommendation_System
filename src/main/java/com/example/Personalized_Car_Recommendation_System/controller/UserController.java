package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.service.UserService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
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

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        // 对密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userService.addUser(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId,
                                        @RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody Map<String, Object> request) {
        String token = authorizationHeader.replace("Bearer ", "");
        try {
            Integer tokenUserId = JwtUtil.getUserIdFromToken(token);
            // 验证路径中的 userId 和 token 中的 userId 是否一致
            if (!userId.equals(tokenUserId)) {
                logger.warn("Path User ID 和 Token User ID 不一致: Path User ID = {}, Token User ID = {}", userId, tokenUserId);
                return new ResponseEntity<>("未授权，请重新登录", HttpStatus.UNAUTHORIZED);
            }

            // 从请求中获取用户名
            String username = (String) request.get("username");
            if (username == null || username.isEmpty()) {
                return new ResponseEntity<>("用户名不能为空", HttpStatus.BAD_REQUEST);
            }

            // 更新用户信息
            User updatedUser = userService.updateUsername(userId, username);
            if (updatedUser != null) {
                return new ResponseEntity<>(updatedUser, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("用户不存在", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("更新用户信息时出现异常: ", e);
            return new ResponseEntity<>("更新用户信息失败，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword) {
        return userService.searchUsers(keyword);
    }
}