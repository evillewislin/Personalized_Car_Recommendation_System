package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarInfoRepository extends JpaRepository<CarInfo, Integer> {
    @Query("SELECT c FROM CarInfo c WHERE c.brandId = :brandId")
    List<CarInfo> findByBrandId(@Param("brandId") Integer brandId);
}
