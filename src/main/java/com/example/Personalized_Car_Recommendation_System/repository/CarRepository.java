package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<CarDetailsDto, Integer> {
    // 可添加自定义查询（如按价格、名称筛选）
}