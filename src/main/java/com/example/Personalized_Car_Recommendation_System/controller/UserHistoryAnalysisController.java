package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import com.example.Personalized_Car_Recommendation_System.entity.User;
import com.example.Personalized_Car_Recommendation_System.repository.RecommendationHistoryRepository;
import com.example.Personalized_Car_Recommendation_System.repository.UserRepository;
import com.example.Personalized_Car_Recommendation_System.service.UserHistoryAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history") // 修改类级路径，避免与UserController冲突
public class UserHistoryAnalysisController {

    @Autowired
    private UserHistoryAnalysisService userHistoryAnalysisService;

    @Autowired
    private RecommendationHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user-history-analysis")
    public List<Object[]> getUserHistoryAnalysis(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return userHistoryAnalysisService.getAnalysisDataByUserId(userId);
        } else {
            return userHistoryAnalysisService.getAnalysisData();
        }
    }

    // 修改路径为 /api/history/users，避免与UserController的/api/users冲突
    @GetMapping("/users")
    public List<User> getUsersForHistoryAnalysis() {
        return userRepository.findAll();
    }

    @GetMapping("/recommendation-history")
    public List<RecommendationHistory> getRecommendationHistory() {
        return historyRepository.findAll();
    }
}