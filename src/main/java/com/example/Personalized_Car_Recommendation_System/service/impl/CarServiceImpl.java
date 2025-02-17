package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.repository.CarBrandRepository;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
@Service // 标记为Spring的Service组件
public class CarServiceImpl implements CarService {

    @Autowired // 自动注入CarBrandRepository
    private CarBrandRepository carBrandRepository;

    @Autowired // 自动注入CarInfoRepository
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

    @Override
    public void deleteCar(Integer id) {
        carBrandRepository.deleteById(id);
    }
}