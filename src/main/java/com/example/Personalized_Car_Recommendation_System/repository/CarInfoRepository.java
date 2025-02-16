package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
public interface CarInfoRepository extends JpaRepository<CarInfo, Integer> {
    @Query("SELECT b.name, i.fullName, i.minPrice, i.maxPrice, b.img " +
            "FROM CarBrand b JOIN CarInfo i ON b.id = i.brandId")
    Page<Object[]> findCarDetailsWithBrand(Pageable pageable);
}
