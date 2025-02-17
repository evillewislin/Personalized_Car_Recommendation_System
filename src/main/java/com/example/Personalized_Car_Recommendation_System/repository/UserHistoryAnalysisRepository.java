package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserHistoryAnalysisRepository extends JpaRepository<RecommendationHistory, Integer> {
    @Query("SELECT rh.timestamp, cb.name FROM RecommendationHistory rh " +
            "JOIN CarInfo ci ON rh.carId = ci.id " +
            "JOIN CarBrand cb ON ci.brandId = cb.id")
    List<Object[]> getAnalysisData();
}