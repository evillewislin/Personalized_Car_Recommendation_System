package com.example.Personalized_Car_Recommendation_System.dto;

import java.io.Serializable;

public class ImCarDetailsDto implements Serializable {
    private Integer carId;
    private String brandName;
    private String fullName;
    private Integer minPrice;
    private Integer maxPrice;

    public ImCarDetailsDto(Integer carId, String brandName, String fullName,  Integer minPrice, Integer maxPrice) {
        this.carId = carId;
        this.brandName = brandName;
        this.fullName = fullName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    // Getters and Setters
    public Integer getCarId() {
        return carId;
    }

    public void setCarId(Integer carId) {
        this.carId = carId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }


}