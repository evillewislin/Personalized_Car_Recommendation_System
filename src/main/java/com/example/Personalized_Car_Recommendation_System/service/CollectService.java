package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;

public interface CollectService {
    RecommendationHistory collectCar(Integer carId, Integer userId, String name, Integer score);
}