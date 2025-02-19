package com.example.Personalized_Car_Recommendation_System.controller;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.dto.CarUpdateDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import com.example.Personalized_Car_Recommendation_System.repository.CarBrandRepository;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private static final Logger log = LogManager.getLogger(CarController.class);
    @Autowired
    private CarService carService;

    @Autowired
    private CarInfoRepository carInfoRepository;

    @Autowired
    private CarBrandRepository carBrandRepository;

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
            log.error("e: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 新增车型
    @PostMapping("/add")
    public ResponseEntity<CarBrand> saveCar(@RequestBody CarUpdateDto carUpdateDto) {
        CarInfo carInfo = carUpdateDto.getCarInfo();
        CarBrand carBrand = carUpdateDto.getCarBrand();

        // 保存 CarBrand
        CarBrand savedCarBrand = carBrandRepository.save(carBrand);

        // 设置 CarInfo 的 brandId
        carInfo.setBrandId(savedCarBrand.getBrandId());

        // 保存 CarInfo
        carInfoRepository.save(carInfo);

        return ResponseEntity.ok(savedCarBrand);
    }

    // 根据ID删除车型
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Integer id) {
        carService.deleteCar(id);
        return ResponseEntity.ok().build();
    }

    // 编辑汽车接口
    @PutMapping("/{carId}")
    public ResponseEntity<Void> updateCar(@PathVariable Integer carId, @RequestBody CarUpdateDto carUpdateDto) {
        CarInfo carInfo = carUpdateDto.getCarInfo();
        CarBrand carBrand = carUpdateDto.getCarBrand();

        // 更新 CarInfo 表
        Optional<CarInfo> optionalCarInfo = carInfoRepository.findById(carId);
        if (optionalCarInfo.isPresent()) {
            CarInfo existingCarInfo = optionalCarInfo.get();
            existingCarInfo.setFullName(carInfo.getFullName());
            existingCarInfo.setMinPrice(carInfo.getMinPrice());
            existingCarInfo.setMaxPrice(carInfo.getMaxPrice());

            // 确保设置 brandId
            Integer brandId = carInfo.getBrandId();
            if (brandId == null) {
                // 如果 brandId 为 null，从现有 CarInfo 中获取
                brandId = existingCarInfo.getBrandId();
            }
            existingCarInfo.setBrandId(brandId);

            carInfoRepository.save(existingCarInfo);
        }
        log.info("carId: {} carBrand: {}", carId, carBrand);

        // 更新 CarBrand 表
        if (carInfo.getBrandId() != null) {
            Optional<CarBrand> optionalCarBrand = carBrandRepository.findById(carInfo.getBrandId());
            if (optionalCarBrand.isPresent()) {
                CarBrand existingCarBrand = optionalCarBrand.get();
                existingCarBrand.setName(carBrand.getName());
                carBrandRepository.save(existingCarBrand);
            }
        }

        return ResponseEntity.ok().build();
    }
}