package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import com.example.Personalized_Car_Recommendation_System.repository.CollectRepository;
import com.example.Personalized_Car_Recommendation_System.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CollectServiceImpl implements CollectService {

    @Autowired
    private CollectRepository collectRepository;

    @Override
    public RecommendationHistory collectCar(Integer carId, Integer userId, String name, Float score) {
        System.out.println("Car ID to save: " + carId);
        RecommendationHistory recommendationHistory = new RecommendationHistory();
        recommendationHistory.setUserId(userId);
        recommendationHistory.setCarId(carId);
        recommendationHistory.setCarName(name);
        recommendationHistory.setScore(score);
        recommendationHistory.setTimestamp(new Date());
        return collectRepository.save(recommendationHistory);
    }
}