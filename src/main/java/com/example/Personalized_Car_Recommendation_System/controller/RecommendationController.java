package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;

    private final JdbcTemplate jdbcTemplate;

    public RecommendationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/recommend")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations(
            @RequestHeader("Authorization") String token) {
        System.out.println("token: " + token);
        // 1. 从token中解析用户ID
        int userId = recommendationService.getUserIdFromToken(token.replace("Bearer ", ""));
        // 2. 查询推荐历史并关联汽车信息
        String sql = """
            SELECT 
                b.name,
                ci.full_name AS fullName,
                CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange,
                b.img AS imageUrl,
                AVG(rh.score) AS avgScore
            FROM recommendation_history rh
            JOIN car_info ci ON rh.car_id = ci.car_id
            JOIN car_brand b ON ci.brand_id = b.brand_id
            WHERE rh.user_id = ?
            GROUP BY b.name, ci.full_name, ci.minprice, ci.maxprice, b.img
            ORDER BY avgScore DESC
            LIMIT 10
            """;

        List<Map<String, Object>> recommendations = jdbcTemplate.queryForList(sql, userId);
        return ResponseEntity.ok(recommendations);
    }
}