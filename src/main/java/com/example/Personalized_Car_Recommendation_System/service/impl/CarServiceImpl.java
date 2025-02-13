package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.repository.CarBrandRepository;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CarServiceImpl implements CarService {

    @Autowired
    private CarBrandRepository carBrandRepository;

    @Override
    public CarBrand saveCar(CarBrand car) {
        return carBrandRepository.save(car);
    }

    @Override
    public List<CarBrand> getAllCars() {
        return carBrandRepository.findAll();
    }

    @Override
    public CarBrand getCarById(Integer id) {
        return carBrandRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteCar(Integer id) {
        carBrandRepository.deleteById(id);
    }
}