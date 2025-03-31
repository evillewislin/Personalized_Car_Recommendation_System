package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import com.example.Personalized_Car_Recommendation_System.service.CollectService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CollectController {

    private static final Logger logger = LoggerFactory.getLogger(CollectController.class);

    @Autowired
    private CollectService collectService;

    @PostMapping("/collect")
    public ResponseEntity<String> collectCar(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody Map<String, Object> request) {
        String token = authorizationHeader.replace("Bearer ", "");
        try {
            Integer userId = JwtUtil.getUserIdFromToken(token);

            Integer carId = (Integer) request.get("carId");
            logger.info("Received Car ID: {}", carId);
            String name = (String) request.get("name");
            Float score = (Float) request.get("score");

            RecommendationHistory result = collectService.collectCar(carId, userId, name, score);
            if (result != null) {
                return new ResponseEntity<>("收藏成功", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("收藏失败", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("解析 token 出错", e);
            return new ResponseEntity<>("收藏出错，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}