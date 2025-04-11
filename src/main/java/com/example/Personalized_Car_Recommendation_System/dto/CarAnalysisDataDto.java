package com.example.Personalized_Car_Recommendation_System.dto;

public class CarAnalysisDataDto {
    private String car_brand_name;
    private Integer minPrice;
    private Integer maxPrice;

    public CarAnalysisDataDto(String car_brand_name, Integer minPrice, Integer maxPrice) {
        this.car_brand_name = car_brand_name;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getCar_brand_name() {
        return car_brand_name;
    }

    public void setCar_brand_name(String car_brand_name) {
        this.car_brand_name = car_brand_name;
    }

    public Integer getMinprice() {
        return minPrice;
    }

    public void setMinprice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxprice() {
        return maxPrice;
    }

    public void setMaxprice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }
}