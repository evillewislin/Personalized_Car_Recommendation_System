package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.service.UserHistoryAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-history-analysis")
public class UserHistoryAnalysisController {
    @Autowired
    private UserHistoryAnalysisService userHistoryAnalysisService;

    @GetMapping
    public List<Object[]> getAnalysisData() {
        return userHistoryAnalysisService.getAnalysisData();
    }
}