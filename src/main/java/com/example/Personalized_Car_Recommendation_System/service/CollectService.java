package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;

public interface CollectService {
    boolean isDuplicateCollect(Integer userId, Integer carId);
    RecommendationHistory collectCar(Integer carId, Integer userId, String name, Float score);
}