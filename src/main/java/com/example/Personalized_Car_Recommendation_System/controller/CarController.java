package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @Autowired
    private CarService carService;

    // 获取所有车型
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> getAllCars(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);  // Spring页码从0开始
        Page<CarDetailsDto> carPage = carService.getAllCarDetails(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", carPage.getContent());
        response.put("total", carPage.getTotalElements());

        return ResponseEntity.ok(response);
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