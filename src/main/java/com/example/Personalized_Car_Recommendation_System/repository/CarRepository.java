package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Set;

public interface CarRepository extends JpaRepository<CarInfo, Integer> {
    // 可以添加自定义查询方法
    @Query("SELECT b.name FROM CarBrand b WHERE b.brandId = :brandId")
    String getBrandNameByBrandId(@Param("brandId") Integer brandId);

    @Query("SELECT b.brandId, b.name FROM CarBrand b WHERE b.brandId IN :brandIds")
    Map<Integer, String> getBrandNameMap(@Param("brandIds") Set<Integer> brandIds);
}