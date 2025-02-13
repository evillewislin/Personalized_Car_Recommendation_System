package com.example.Personalized_Car_Recommendation_System.repository;


import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Integer> {
    RecommendationHistory findByUserId(Integer userId);
}