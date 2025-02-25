package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RecommendationService {
    int getUserIdFromToken(String token);

    CompletableFuture<String> callAI(String message);


    List<Map<String, Object>> getAlsRecommendations(int userId, List<Map<String, Object>> data);

    Map<String, Object> getRecommendations(int userId, int page, int size, String keyword);

    Map<String, Object> getAllRecommendations(int userId);
}