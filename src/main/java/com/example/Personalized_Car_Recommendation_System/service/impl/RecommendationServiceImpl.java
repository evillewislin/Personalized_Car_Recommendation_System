package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.entity.Car;
import com.example.Personalized_Car_Recommendation_System.entity.UserPreference;
import com.example.Personalized_Car_Recommendation_System.repository.CarRepository;
import com.example.Personalized_Car_Recommendation_System.repository.UserPreferenceRepository;
import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Override
    public List<Car> getRecommendationsForUser(Integer userId) {
        UserPreference pref = userPreferenceRepository.findByUserId(userId);
        List<Car> allCars = carRepository.findAll();
        List<Car> recommendations = new ArrayList<>();
        if (pref != null && pref.getMinPrice() != null && pref.getMaxPrice() != null) {
            for (Car car : allCars) {
                if (car.getPrice() >= pref.getMinPrice().doubleValue() &&
                        car.getPrice() <= pref.getMaxPrice().doubleValue()) {
                    recommendations.add(car);
                }
            }
        } else {
            recommendations = allCars;
        }
        // 这里可以接入 ALS 算法进行协同过滤重排
        return recommendations;
    }

    @Override
    public void updateRecommendations() {
        // 定时任务中调用 ALS 算法更新推荐结果（此处为示例）
    }
}