package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import com.example.Personalized_Car_Recommendation_System.repository.UserHistoryAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserHistoryAnalysisService {
    @Autowired
    private UserHistoryAnalysisRepository userHistoryAnalysisRepository;

    public List<Object[]> getAnalysisData() {
        return userHistoryAnalysisRepository.getAnalysisData();
    }
}


