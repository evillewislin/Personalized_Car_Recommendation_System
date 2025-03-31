package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RecommendationService {
    int getUserIdFromToken(String token);

    CompletableFuture<String> callAI(String message);

    // 添加 maxPrice 参数
    List<Map<String, Object>> getAlsRecommendations(int userId, List<Map<String, Object>> data, int maxPrice);

    Map<String, Object> getRecommendations(int userId, int page, int size, String keyword);

    Map<String, Object> getAllRecommendations(int userId);

    @Cacheable(value = "recommendations", key = "#rank+#iterations+#lambda+#page+#size")
    Page<CarDetailsDto> generateExplicitRecommendations(int rank, int iterations, double lambda, int maxPrice,int page, int size);

    @Cacheable(value = "implicitRecommendations", key = "#rank+#iterations+#lambda+#maxPrice+#pageable")
    Page<CarDetailsDto> generateImplicitRecommendations(
            int rank,
            int iterations,
            double lambda,
            int maxPrice,
            int page, int size);
}