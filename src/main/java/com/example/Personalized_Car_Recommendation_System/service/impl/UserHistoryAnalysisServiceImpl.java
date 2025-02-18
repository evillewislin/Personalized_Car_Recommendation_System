package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.repository.UserHistoryAnalysisRepository;
import com.example.Personalized_Car_Recommendation_System.service.UserHistoryAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserHistoryAnalysisServiceImpl implements UserHistoryAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(UserHistoryAnalysisServiceImpl.class);
    private final UserHistoryAnalysisRepository userHistoryAnalysisRepository;

    @Autowired
    public UserHistoryAnalysisServiceImpl(UserHistoryAnalysisRepository userHistoryAnalysisRepository) {
        this.userHistoryAnalysisRepository = userHistoryAnalysisRepository;
    }

    @Override
    public List<Object[]> getAnalysisData() {
        try {
            return userHistoryAnalysisRepository.getAnalysisData();
        } catch (Exception e) {
            logger.error("Failed to get analysis data", e);
            return null;
        }
    }

    @Override
    public List<Object[]> getAnalysisDataByUserId(Integer userId) {
        try {
            return userHistoryAnalysisRepository.getAnalysisDataByUserId(userId);
        } catch (Exception e) {
            logger.error("Failed to get analysis data by user ID: {}", userId, e);
            return null;
        }
    }
}