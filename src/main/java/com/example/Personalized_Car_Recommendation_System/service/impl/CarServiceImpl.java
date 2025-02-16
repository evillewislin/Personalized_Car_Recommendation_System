package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import com.example.Personalized_Car_Recommendation_System.repository.CarBrandRepository;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        // 使用联表查询并分页
        Page<Object[]> results = carInfoRepository.findCarDetailsWithBrand(pageable);

        // 将结果转换为CarDetailsDto列表
        List<CarDetailsDto> content = results.getContent().stream()
                .map(arr -> new CarDetailsDto(
                        (String) arr[0],   // brandName
                        (String) arr[1],   // fullName
                        (Integer) arr[2],  // minPrice
                        (Integer) arr[3],  // maxPrice
                        (String) arr[4]    // img
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, results.getTotalElements());
    }


    @Override
    public CarBrand saveCar(CarBrand car) {
        return null;
    }

    @Override
    public void deleteCar(Integer id) {

    }
}