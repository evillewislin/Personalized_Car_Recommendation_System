package com.example.Personalized_Car_Recommendation_System.service.impl;

import com.example.Personalized_Car_Recommendation_System.dto.CarDetailsDto;
import com.example.Personalized_Car_Recommendation_System.entity.CarBrand;
import com.example.Personalized_Car_Recommendation_System.entity.CarInfo;
import com.example.Personalized_Car_Recommendation_System.repository.CarBrandRepository;
import com.example.Personalized_Car_Recommendation_System.repository.CarInfoRepository;
import com.example.Personalized_Car_Recommendation_System.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service // 标记为Spring的Service组件
public class CarServiceImpl implements CarService {

    @Autowired // 自动注入CarBrandRepository
    private CarBrandRepository carBrandRepository;

    @Autowired // 自动注入CarInfoRepository
    private CarInfoRepository carInfoRepository;


    @Override
    public List<CarDetailsDto> getAllCarDetails() {
        // 查询所有品牌信息
        List<CarBrand> carBrands = carBrandRepository.findAll();

        // 将品牌信息与车型信息组合成DTO
        return carBrands.stream()
                .map(carBrand -> {
                    // 根据品牌ID查询车型信息
                    CarInfo carInfo = carInfoRepository.findByBrandId(carBrand.getId());

                    // 构建DTO对象
                    return new CarDetailsDto(
                            carBrand.getName(),
                            carInfo.getFullName(),
                            carInfo.getMinPrice(),
                            carInfo.getMaxPrice(),
                            carBrand.getImg()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public CarBrand saveCar(CarBrand car) {
        return null;
    }

    @Override
    public void deleteCar(Integer id) {

    }
}