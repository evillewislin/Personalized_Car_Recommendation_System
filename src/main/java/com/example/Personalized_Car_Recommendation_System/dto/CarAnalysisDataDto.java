package com.example.Personalized_Car_Recommendation_System.dto;

public class CarAnalysisDataDto {
    private String car_brand_name;
    private Integer minprice;
    private Integer maxprice;

    public CarAnalysisDataDto(String car_brand_name, Integer minprice, Integer maxprice) {
        this.car_brand_name = car_brand_name;
        this.minprice = minprice;
        this.maxprice = maxprice;
    }

    public String getCar_brand_name() {
        return car_brand_name;
    }

    public void setCar_brand_name(String car_brand_name) {
        this.car_brand_name = car_brand_name;
    }

    public Integer getMinprice() {
        return minprice;
    }

    public void setMinprice(Integer minprice) {
        this.minprice = minprice;
    }

    public Integer getMaxprice() {
        return maxprice;
    }

    public void setMaxprice(Integer maxprice) {
        this.maxprice = maxprice;
    }
}