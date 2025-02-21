package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarAnalysisDataDto;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import com.example.Personalized_Car_Recommendation_System.service.CarAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarAnalysisServiceImpl implements CarAnalysisService {

    @Autowired
    private CarInfoRepository carInfoRepository;

    @Override
    public List<CarAnalysisDataDto> getCarAnalysisData() {
        return carInfoRepository.getCarAnalysisData();
    }

    @Override
    public List<CarAnalysisDataDto> getCarAnalysisDataByMaxPrice(Integer maxPrice) {
        return carInfoRepository.getCarAnalysisDataByMaxPrice(maxPrice);
    }
}