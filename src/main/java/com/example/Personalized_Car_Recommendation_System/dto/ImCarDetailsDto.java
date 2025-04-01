package com.example.Personalized_Car_Recommendation_System.dto;

import java.io.Serializable;

public class ImCarDetailsDto implements Serializable {
    private Integer carId;
    private String brandName;
    private String fullName;
    private String priceRange;
    private double predictedRating;

    public ImCarDetailsDto(Integer carId, String brandName, String fullName, String priceRange, double predictedRating) {
        this.carId = carId;
        this.brandName = brandName;
        this.fullName = fullName;
        this.priceRange = priceRange;
        this.predictedRating = predictedRating;
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

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public double getPredictedRating() {
        return predictedRating;
    }

    public void setPredictedRating(double predictedRating) {
        this.predictedRating = predictedRating;
    }


}