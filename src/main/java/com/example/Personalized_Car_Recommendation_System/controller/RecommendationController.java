package com.example.Personalized_Car_Recommendation_System.controller;



import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/ai")
public class RecommendationController {
    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;
    private final RecommendationService recommendationService;

    public RecommendationController(ChatClient.Builder builder, JdbcTemplate jdbcTemplate, RecommendationService recommendationService) {
        this.chatClient = builder.build();
        this.jdbcTemplate = jdbcTemplate;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<List<Map<String, Object>>> chat(@RequestHeader("Authorization") String token) {

        try {
            int userId = recommendationService.getUserIdFromToken(token.replace("Bearer ", ""));
            String sql = """
                SELECT b.name, ci.full_name AS fullName,
                       CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange,
                       b.img AS imageUrl,
                       AVG(rh.score) AS avgScore
                FROM recommendation_history rh
                JOIN car_info ci ON rh.car_id = ci.id
                JOIN car_brand b ON ci.brand_id = b.id
                WHERE rh.user_id = ?
                GROUP BY b.name, ci.full_name, ci.minprice, ci.maxprice, b.img
                ORDER BY avgScore DESC
                LIMIT 10
            """;
            List<Map<String, Object>> recommendations = jdbcTemplate.queryForList(sql, userId);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}