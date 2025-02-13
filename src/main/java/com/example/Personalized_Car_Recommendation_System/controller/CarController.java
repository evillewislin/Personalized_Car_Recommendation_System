package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @Autowired
    private CarService carService;

    // 获取所有车型
    @GetMapping
    public ResponseEntity<List<CarBrand>> getAllCars() {
        List<CarBrand> cars = carService.getAllCars();
        return ResponseEntity.ok(cars);
    }

    // 根据ID获取单个车型信息
    @GetMapping("/{id}")
    public ResponseEntity<CarBrand> getCarById(@PathVariable Integer id) {
        CarBrand car = carService.getCarById(id);
        if (car != null) {
            return ResponseEntity.ok(car);
        }
        return ResponseEntity.notFound().build();
    }

    // 新增或更新车型
    @PostMapping
    public ResponseEntity<CarBrand> saveCar(@RequestBody CarBrand car) {
        CarBrand savedCar = carService.saveCar(car);
        return ResponseEntity.ok(savedCar);
    }

    // 根据ID删除车型
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Integer id) {
        carService.deleteCar(id);
        return ResponseEntity.ok().build();
    }
}