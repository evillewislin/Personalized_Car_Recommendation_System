package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.service.UserService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

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
        return userService.addUser(user);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String authorizationHeader,
                                           @RequestBody Map<String, Object> request) {
        String token = authorizationHeader.replace("Bearer ", "");
        try {
            Integer userId = JwtUtil.getUserIdFromToken(token);
            User user = userService.getUserById(userId);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // 更新用户信息
            if (request.containsKey("name")) {
                user.setUsername((String) request.get("name"));
            }
            if (request.containsKey("oldPassword")) {
                user.setOldPassword((String) request.get("oldPassword"));
            }
            if (request.containsKey("newPassword")) {
                user.setNewPassword((String) request.get("newPassword"));
            }

            User updatedUser = userService.updateUser(user);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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