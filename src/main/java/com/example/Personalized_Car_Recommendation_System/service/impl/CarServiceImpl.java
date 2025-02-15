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

        // 将品牌信息与车型信息组合成DTO列表
        List<CarDetailsDto> carDetailsDtos = new ArrayList<>();

        for (CarBrand carBrand : carBrands) {
            // 根据品牌ID查询所有车型信息
            List<CarInfo> carInfos = carInfoRepository.findByBrandId(carBrand.getId());

            for (CarInfo carInfo : carInfos) {
                // 构建DTO对象
                CarDetailsDto carDetailsDto = new CarDetailsDto(
                        carBrand.getName(),
                        carInfo.getFullName(),
                        carInfo.getMinPrice(),
                        carInfo.getMaxPrice(),
                        carBrand.getImg()
                );
                carDetailsDtos.add(carDetailsDto);
            }
        }

        return carDetailsDtos;
    }


    @Override
    public CarBrand saveCar(CarBrand car) {
        return null;
    }

    @Override
    public void deleteCar(Integer id) {

    }
}