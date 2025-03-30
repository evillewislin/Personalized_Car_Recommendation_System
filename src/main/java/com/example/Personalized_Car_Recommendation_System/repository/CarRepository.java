package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarRepository extends JpaRepository<CarInfo, Integer> {
    // 可以添加自定义查询方法
    @Query("SELECT b.name FROM CarBrand b WHERE b.brandId = :brandId")
    String getBrandNameByBrandId(@Param("brandId") Integer brandId);
}