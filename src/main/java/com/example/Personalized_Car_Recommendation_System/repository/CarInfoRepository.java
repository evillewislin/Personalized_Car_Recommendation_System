package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.dto.CarAnalysisDataDto;
import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarInfoRepository extends JpaRepository<CarInfo, Integer> {
    @Query("SELECT i.id ,b.name AS brandName, i.fullName AS fullName, i.minPrice AS minPrice, " +
            "i.maxPrice AS maxPrice " +
            "FROM CarBrand b " +
            "JOIN CarInfo i ON b.brandId = i.brandId")
    Page<Object[]> findCarDetailsWithBrand(Pageable pageable);

    @Query("SELECT new com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto(i.id,b.name, i.fullName, i.minPrice, i.maxPrice) " +
            "FROM CarBrand b " +
            "JOIN CarInfo i ON b.brandId = i.brandId " +
            "WHERE b.name LIKE %:keyword% OR i.fullName LIKE %:keyword%")
    Page<CarDetailsDto> findAllCarDetailsByKeyword(Pageable pageable, @Param("keyword") String keyword);

    @Query("SELECT new com.example.Personalized_Car_Recommendation_System.dto.CarAnalysisDataDto(cb.name, ci.minPrice, ci.maxPrice) " +
            "FROM CarInfo ci " +
            "JOIN CarBrand cb ON ci.brandId = cb.brandId")
    List<CarAnalysisDataDto> getCarAnalysisData();

    @Query("SELECT new com.example.Personalized_Car_Recommendation_System.dto.CarAnalysisDataDto(cb.name, ci.minPrice, ci.maxPrice) " +
            "FROM CarInfo ci " +
            "JOIN CarBrand cb ON ci.brandId = cb.brandId " +
            "WHERE ci.maxPrice <= :maxPrice")
    List<CarAnalysisDataDto> getCarAnalysisDataByMaxPrice(@Param("maxPrice") Integer maxPrice);

    }