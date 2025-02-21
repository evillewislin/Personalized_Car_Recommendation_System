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
     * 读取用户历史数据
     * @param token 用户的授权令牌
     * @return 推荐信息列表
     */
    @PostMapping("/recommend")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations(@RequestHeader("Authorization") String token) {
        try {
            int userId = getUserIdFromToken(token);
            List<Map<String, Object>> recommendations = recommendationService.getRecommendationsByUserId(userId);
            return ResponseEntity.ok(recommendations);
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