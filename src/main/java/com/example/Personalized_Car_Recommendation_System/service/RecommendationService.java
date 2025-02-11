package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.entity.Car;
import java.util.List;

public interface RecommendationService {
    List<Car> getRecommendationsForUser(Integer userId);
    void updateRecommendations();
}