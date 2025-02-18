package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;

import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import org.springframework.data.domain.*;

public interface CarService {
    Page<CarDetailsDto> getAllCarDetails(Pageable pageable);
    Page<CarDetailsDto> getAllCarDetails(Pageable pageable, String keyword);
    CarBrand saveCar(CarBrand car);

    void deleteCar(Integer id);
    void updateCarInfoAndBrand(Integer carId, CarInfo carInfo, CarBrand carBrand);
}