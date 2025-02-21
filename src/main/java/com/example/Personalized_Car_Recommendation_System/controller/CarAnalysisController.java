package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.dto.CarAnalysisDataDto;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CarAnalysisController {

    @Autowired
    private CarInfoRepository carInfoRepository;

    @GetMapping("/car-analysis")
    public List<CarAnalysisDataDto> getCarAnalysisData(@RequestParam(required = false) Integer maxPrice) {
        try {
            if (maxPrice != null) {
                return carInfoRepository.getCarAnalysisDataByMaxPrice(maxPrice);
            }
            return carInfoRepository.getCarAnalysisData();
        } catch (Exception e) {
            // 这里可以根据具体情况进行更详细的日志记录和异常处理
            System.err.println("获取汽车分析数据时发生错误: " + e.getMessage());
            return List.of();
        }
    }
}