package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @Autowired
    private CarService carService;

    // 获取所有车型
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> getAllCars(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        // 将前端1-based页码转换为Spring Data的0-based
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<CarDetailsDto> carPage;

        if (keyword != null && !keyword.isEmpty()) {
            // 如果有搜索关键词，调用带关键词的查询方法
            carPage = carService.getAllCarDetails(pageable, keyword);
        } else {
            // 没有搜索关键词，调用默认的查询方法
            carPage = carService.getAllCarDetails(pageable);
        }

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("data", carPage.getContent());
            response.put("total", carPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
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