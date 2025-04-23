package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.ImCarDetailsDto;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RecommendationService {
    int getUserIdFromToken(String token);

    CompletableFuture<String> callAI(String message);

    List<Map<String, Object>> getAlsRecommendations(int userId, List<Map<String, Object>> data, int maxPrice);

    Map<String, Object> getRecommendations(int userId, int page, int size, String keyword);

    Map<String, Object> getAllRecommendations(int userId);


    @Cacheable(value = "recommendations", key = "#rank+#iterations+#lambda+#maxPrice+#userId")
    List<CarDetailsDto> generateExplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int maxPrice,
            Integer userId);

    @Cacheable(value = "implicitRecommendations", key = "#rank+#iterations+#lambda+#maxPrice+#userId")
    List<ImCarDetailsDto> generateImplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int maxPrice,
            int userId );

}