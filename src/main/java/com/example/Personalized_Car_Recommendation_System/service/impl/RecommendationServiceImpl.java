package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import com.example.Personalized_Car_Recommendation_System.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    /**
     * 从令牌中获取用户ID
     * @param token 用户的令牌
     * @return 用户ID
     */
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

    /**
     * 异步调用AI接口
     * @param prompt 提示信息
     * @return 异步的AI响应
     */
    @Async
    @Override
    public CompletableFuture<String> callAI(Prompt prompt) {
        try {
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            logger.error("AI 调用异常: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 根据用户ID获取推荐信息
     * @param userId 用户ID
     * @return 推荐信息列表
     */
    @Override
    public List<Map<String, Object>> getRecommendationsByUserId(int userId) {
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
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
        logger.info("查询结果数量: {}", result.size());
        return result;
    }
}