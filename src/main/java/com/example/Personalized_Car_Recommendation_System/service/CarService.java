package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;

import java.util.List;

public interface CarService {
    List<CarDetailsDto> getAllCarDetails();

    CarBrand saveCar(CarBrand car);

    void deleteCar(Integer id);
}