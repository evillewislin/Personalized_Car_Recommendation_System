package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
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
    @GetMapping("/search")
    public ResponseEntity<List<CarDetailsDto>> getAllCars(@RequestHeader(value = "Authorization", required = false) String token) {
        System.out.println("GET /api/cars/search request received");
        if (token != null) {
            System.out.println("Received token: " + token);  // 打印接收到的 token，进行调试
        }
        List<CarDetailsDto> cars = carService.getAllCarDetails();
        return ResponseEntity.ok(cars);
    }


    // 新增或更新车型
    @PostMapping("/add")
    public ResponseEntity<CarBrand> saveCar(@RequestBody CarBrand car) {
        CarBrand savedCar = carService.saveCar(car);
        return ResponseEntity.ok(savedCar);
    }

    // 根据ID删除车型
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Integer id) {
        carService.deleteCar(id);
        return ResponseEntity.ok().build();
    }
}