package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface CarRepository extends JpaRepository<CarInfo, Integer> {
    @Query("SELECT b.name FROM CarBrand b WHERE b.brandId = :brandId")
    String getBrandNameByBrandId(@Param("brandId") Integer brandId);


    @Query("SELECT DISTINCT c FROM CarInfo c JOIN RecommendationHistory p ON c.id = p.carId WHERE p.userId IN :userIds AND c.maxPrice <= :maxPrice")
    List<CarInfo> findCarsHistoryByUsers(
            @Param("userIds") List<Long> userIds,
            @Param("maxPrice") int maxPrice);

}
