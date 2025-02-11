package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Integer> {
    List<RecommendationHistory> findByUserId(Integer userId);
}