package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final JwtUtil jwtUtil;
    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RecommendationServiceImpl(JwtUtil jwtUtil, ChatClient chatClient, JdbcTemplate jdbcTemplate) {
        this.jwtUtil = jwtUtil;
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int getUserIdFromToken(String token) {
        logger.info("开始解析 Token: {}", token);
        try {
            Integer userId = JwtUtil.getUserIdFromToken(token);
            logger.debug("成功解析 Token，用户 ID: {}", userId);
            return userId;
        } catch (Exception e) {
            logger.error("Token 解析异常: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Token 解析失败", e);
        }
    }

    @Async
    public CompletableFuture<String> callAI(Prompt prompt) {
        try {
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            logger.error("AI 调用异常: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public List<Map<String, Object>> getRecommendationsByUserId(int userId) {
        // 这里实现具体的业务逻辑，例如从数据库查询用户的推荐信息
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
        return jdbcTemplate.queryForList(sql, userId);
    }
}