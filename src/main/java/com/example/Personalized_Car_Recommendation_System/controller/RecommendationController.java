package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
public class RecommendationController {
    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
    private final RecommendationService recommendationService;
    private final ChatClient chatClient;

    public RecommendationController(RecommendationService recommendationService, ChatClient chatClient) {
        this.recommendationService = recommendationService;
        this.chatClient = chatClient;
    }

    /**
     * 提取从 Token 中获取用户 ID 的逻辑
     * @param token 用户的令牌
     * @return 用户ID
     */
    private int getUserIdFromToken(String token) {
        return recommendationService.getUserIdFromToken(token.replace("Bearer ", ""));
    }

    /**
     * 读取用户历史数据
     * @param token 用户的授权令牌
     * @return 推荐信息的 JSON 格式
     */
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            log.info("收到请求，Token: {}, 页码: {}, 每页数量: {}, 关键词: {}", cleanToken, page, size, keyword);
            int userId = recommendationService.getUserIdFromToken(cleanToken);
            Map<String, Object> result = recommendationService.getRecommendations(userId, page, size, keyword);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("处理推荐汽车列表请求时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/allrecommend")
    public ResponseEntity<Map<String, Object>> getAllRecommendations(
            @RequestHeader("Authorization") String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            log.info("收到请求，Token: {}", cleanToken);
            int userId = recommendationService.getUserIdFromToken(cleanToken);
            log.info("Processing allrecommend for user: {}", userId);
            Map<String, Object> result = recommendationService.getAllRecommendations(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("处理全部推荐汽车列表请求时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 调用ai接口
     * @param message 用户输入的消息
     * @return 异步的AI响应
     */
    @GetMapping("/chat")
    public CompletableFuture<ResponseEntity<String>> chatWithAI(@RequestParam(value = "message") String message) {
        // 输出向 AI 发送的信息
        log.info("向 AI 发送的信息: {}", message);

        return recommendationService.callAI(message)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("AI chat error: {}", ex.getMessage(), ex);
                    return ResponseEntity.badRequest().build();
                });
    }

    /**
     * 新增：ALS 算法接口
     * @param token 用户的授权令牌
     * @param data 调用 /api/ai/recommend 接口返回的数据
     * @param maxPrice 用户输入的最高价格
     * @return ALS 算法过滤后的数据
     */
    @PostMapping("/als")
    public ResponseEntity<List<Map<String, Object>>> getAlsRecommendations(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Map<String, Object>> data,
            @RequestParam("maxPrice") int maxPrice) {
        log.info("ALS request received with data size: {}, maxPrice: {}", data.size(), maxPrice);
        token = token.replace("Bearer ", "").trim();
        try {
            int userId = recommendationService.getUserIdFromToken(token);
            List<Map<String, Object>> alsRecommendations = recommendationService.getAlsRecommendations(userId, data, maxPrice);
            return ResponseEntity.ok(alsRecommendations);
        } catch (IllegalArgumentException e) {
            return handleTokenParsingError(e);
        } catch (Exception e) {
            return handleInternalServerError(e, "Error getting ALS recommendations");
        }
    }
    @PostMapping("/Ex_cars")
    public ResponseEntity<Page<CarDetailsDto>> getRecommendations(
            @RequestBody Map<String, Object> params,
            @RequestParam int page,
            @RequestParam int size
    ) {
        int rank = ((Number) params.get("rank")).intValue();
        int iterations = ((Number) params.get("iterations")).intValue();
        double lambda = ((Number) params.get("lambda")).doubleValue();
        Page<CarDetailsDto> recommendations = recommendationService.generateExplicitRecommendations(rank, iterations, lambda, page, size);
        return new ResponseEntity<>(recommendations, HttpStatus.OK);
    }
    /**
     * 处理令牌解析错误
     * @param e 异常信息
     * @return 错误响应
     */
    private ResponseEntity<List<Map<String, Object>>> handleTokenParsingError(IllegalArgumentException e) {
        log.error("Token parsing error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    /**
     * 处理内部服务器错误
     * @param e 异常信息
     * @param errorMessage 错误消息
     * @return 错误响应
     */
    private ResponseEntity<List<Map<String, Object>>> handleInternalServerError(Exception e, String errorMessage) {
        log.error("{}: {}", errorMessage, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}