package com.example.Personalized_Car_Recommendation_System.service;

import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;

import java.util.List;

public interface CarService {
    CarBrand saveCar(CarBrand carbrand);
    List<CarBrand> getAllCars();
    CarBrand getCarById(Integer id);
    void deleteCar(Integer id);
}