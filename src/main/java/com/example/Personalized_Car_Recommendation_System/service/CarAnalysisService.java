package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.dto.CarAnalysisDataDto;
import java.util.List;

public interface CarAnalysisService {
    List<CarAnalysisDataDto> getCarAnalysisData();
    List<CarAnalysisDataDto> getCarAnalysisDataByMaxPrice(Integer maxPrice);
}
