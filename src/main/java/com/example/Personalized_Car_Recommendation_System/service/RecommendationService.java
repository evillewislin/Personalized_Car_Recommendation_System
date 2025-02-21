package com.example.Personalized_Car_Recommendation_System.service;

import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RecommendationService {
    int getUserIdFromToken(String token);

    CompletableFuture<String> callAI(Prompt prompt);

    List<Map<String, Object>> getRecommendationsByUserId(int userId);
}