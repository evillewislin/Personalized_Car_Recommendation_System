package com.example.Personalized_Car_Recommendation_System.controller;
import com.example.Personalized_Car_Recommendation_System.service.UserHistoryAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserHistoryAnalysisController {

    @Autowired
    private UserHistoryAnalysisService userHistoryAnalysisService;

    @GetMapping("/user-history-analysis")
    public List<Object[]> getUserHistoryAnalysis(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return userHistoryAnalysisService.getAnalysisDataByUserId(userId);
        } else {
            return userHistoryAnalysisService.getAnalysisData();
        }
    }
}