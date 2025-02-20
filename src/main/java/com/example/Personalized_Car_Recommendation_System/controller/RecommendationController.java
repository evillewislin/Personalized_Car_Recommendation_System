package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
public class RecommendationController {
    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
    private final JdbcTemplate jdbcTemplate;
    private final RecommendationService recommendationService;
    private final ChatClient chatClient;

    public RecommendationController(JdbcTemplate jdbcTemplate, RecommendationService recommendationService, ChatClient chatClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.recommendationService = recommendationService;
        this.chatClient = chatClient;
    }

    // 提取从 Token 中获取用户 ID 的逻辑
    private int getUserIdFromToken(String token) {
        return recommendationService.getUserIdFromToken(token.replace("Bearer ", ""));
    }

    // 读取用户历史数据
    @PostMapping("/recommend")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations(@RequestHeader("Authorization") String token) {
        try {
            int userId = getUserIdFromToken(token);
            String sql = "SELECT b.name, ci.full_name AS fullName, " +
                    "CONCAT(ci.minprice, '-', ci.maxprice) AS priceRange, " +
                    "AVG(rh.score) AS avgScore " +
                    "FROM recommendation_history rh " +
                    "JOIN car_info ci ON rh.car_id = ci.car_id " +
                    "JOIN car_brand b ON ci.brand_id = b.brand_id " +
                    "WHERE rh.user_id = ? " +
                    "GROUP BY b.name, ci.full_name, ci.minprice, ci.maxprice " +
                    "ORDER BY avgScore DESC " +
                    "LIMIT 10";
            List<Map<String, Object>> recommendations = jdbcTemplate.queryForList(sql, userId);

            // 进一步合并 fullName 相同的记录（理论上 SQL 已经处理，但为确保万无一失）
            Map<String, Map<String, Object>> mergedMap = new HashMap<>();
            for (Map<String, Object> recommendation : recommendations) {
                String fullName = (String) recommendation.get("fullName");
                if (mergedMap.containsKey(fullName)) {
                    // 如果已经存在该 fullName 的记录，更新 score
                    Map<String, Object> existing = mergedMap.get(fullName);
                    double existingScore = Double.parseDouble(existing.get("avgScore").toString());
                    double newScore = Double.parseDouble(recommendation.get("avgScore").toString());
                    // 这里由于 SQL 已经计算过平均值，简单覆盖即可
                    existing.put("avgScore", newScore);
                } else {
                    // 不存在则添加到合并结果中
                    mergedMap.put(fullName, new HashMap<>(recommendation));
                }
            }

            List<Map<String, Object>> mergedRecommendations = new ArrayList<>(mergedMap.values());
            return ResponseEntity.ok(mergedRecommendations);
        } catch (IllegalArgumentException e) {
            log.error("Token parsing error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            log.error("Error getting recommendations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 调用ai接口
    @GetMapping("/chat")
    public CompletableFuture<ResponseEntity<String>> chatWithAI(@RequestParam(value = "message") String message) {
        log.info(message);
        Prompt prompt = new Prompt(new UserMessage(message));
        return recommendationService.callAI(prompt)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("AI chat error: {}", ex.getMessage(), ex);
                    return ResponseEntity.badRequest().build();
                });
    }

    // 添加 ALS 接口
    @GetMapping("/als")
    public ResponseEntity<List<Map<String, Object>>> getALSRecommendations(@RequestHeader("Authorization") String token) {
        try {
            log.info("Received token: {}", token);
            int userId = getUserIdFromToken(token);
            List<Map<String, Object>> alsRecommendations = recommendationService.getALSRecommendations(userId);
            return ResponseEntity.ok(alsRecommendations);
        } catch (IllegalArgumentException e) {
            log.error("Token parsing error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            log.error("ALS recommendation error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ChatClient getChatClient() {
        return chatClient;
    }
}