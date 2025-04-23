package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import com.example.Personalized_Car_Recommendation_System.repository.CarBrandRepository;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
@Service
public class CarServiceImpl implements CarService {

    @Autowired
    private CarBrandRepository carBrandRepository;

    @Autowired
    private CarInfoRepository carInfoRepository;


    @Override
    public Page<CarDetailsDto> getAllCarDetails(Pageable pageable) {
        Page<Object[]> results = carInfoRepository.findCarDetailsWithBrand(pageable);

        List<CarDetailsDto> content = results.stream()
                .map(arr -> new CarDetailsDto(
                        (Integer) arr[0],  //carId
                        (String) arr[1],   // brandName
                        (String) arr[2],   // fullName
                        (Integer) arr[3], // minPrice
                        (Integer) arr[4] // maxPrice

                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, results.getTotalElements());
    }
    @Override
    public Page<CarDetailsDto> getAllCarDetails(Pageable pageable, String keyword) {
        // 实现带关键词的分页查询逻辑
        return carInfoRepository.findAllCarDetailsByKeyword(pageable, keyword);
    }
    @Override
    public CarBrand saveCar(CarBrand car) {
        return carBrandRepository.save(car);
    }

    @Transactional
    @Override
    public void deleteCar(Integer carId) {
        // 根据 carId 删除 CarInfo 记录
        Optional<CarInfo> carInfoOptional = carInfoRepository.findById(carId);
        if (carInfoOptional.isPresent()) {
            carInfoRepository.deleteById(carId);
        }
    }



    public void updateCarInfoAndBrand(Integer carId, CarInfo carInfo, CarBrand carBrand) {
        // 更新 CarInfo 表
        Optional<CarInfo> optionalCarInfo = carInfoRepository.findById(carId);
        if (optionalCarInfo.isPresent()) {
            CarInfo existingCarInfo = optionalCarInfo.get();
            existingCarInfo.setFullName(carInfo.getFullName());
            existingCarInfo.setMinPrice(carInfo.getMinPrice());
            existingCarInfo.setMinPrice(carInfo.getMaxPrice());
            carInfoRepository.save(existingCarInfo);
        }

        // 更新 CarBrand 表
        Integer brandId = carInfo.getBrandId();
        if (brandId != null) {
            Optional<CarBrand> optionalCarBrand = carBrandRepository.findById(brandId);
            if (optionalCarBrand.isPresent()) {
                CarBrand existingCarBrand = optionalCarBrand.get();
                existingCarBrand.setName(carBrand.getName());
                carBrandRepository.save(existingCarBrand);
            }
        }
    }
}