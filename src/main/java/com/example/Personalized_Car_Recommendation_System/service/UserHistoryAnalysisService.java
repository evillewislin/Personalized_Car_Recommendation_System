package com.example.Personalized_Car_Recommendation_System.service;

import java.util.List;

public interface UserHistoryAnalysisService {
    List<Object[]> getAnalysisData();
    List<Object[]> getAnalysisDataByUserId(Integer userId);
}