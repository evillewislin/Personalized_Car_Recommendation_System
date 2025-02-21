package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
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
     * 将推荐信息列表转换为文本格式
     * @param recommendations 推荐信息列表
     * @return 文本格式的推荐信息
     */
    private String convertRecommendationsToText(List<Map<String, Object>> recommendations) {
        StringBuilder text = new StringBuilder();
        text.append("品牌名\t全名\t价格范围\t平均评分\n");
        for (Map<String, Object> recommendation : recommendations) {
            String name = (String) recommendation.get("name");
            String fullName = (String) recommendation.get("fullName");
            String priceRange = (String) recommendation.get("priceRange");
            double avgScore = (double) recommendation.get("avgScore");
            text.append(name).append("\t")
                    .append(fullName).append("\t")
                    .append(priceRange).append("\t")
                    .append(avgScore).append("\n");
        }
        return text.toString();
    }

    /**
     * 读取用户历史数据
     * @param token 用户的授权令牌
     * @return 推荐信息的文本格式
     */
    @PostMapping("/recommend")
    public ResponseEntity<String> getRecommendations(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "").trim();
        try {
            int userId = recommendationService.getUserIdFromToken(token);
            List<Map<String, Object>> recommendations = recommendationService.getRecommendationsByUserId(userId);
            String textRecommendations = convertRecommendationsToText(recommendations);
            return ResponseEntity.ok(textRecommendations);
        } catch (IllegalArgumentException e) {
            return handleTokenParsingError(e);
        } catch (Exception e) {
            return handleInternalServerError(e, "Error getting recommendations");
        }
    }

    /**
     * 调用ai接口
     * @param message 用户输入的消息
     * @return 异步的AI响应
     */
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

    /**
     * 处理令牌解析错误
     * @param e 异常信息
     * @return 错误响应
     */
    private ResponseEntity<String> handleTokenParsingError(IllegalArgumentException e) {
        log.error("Token parsing error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    /**
     * 处理内部服务器错误
     * @param e 异常信息
     * @param errorMessage 错误消息
     * @return 错误响应
     */
    private ResponseEntity<String> handleInternalServerError(Exception e, String errorMessage) {
        log.error("{}: {}", errorMessage, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}