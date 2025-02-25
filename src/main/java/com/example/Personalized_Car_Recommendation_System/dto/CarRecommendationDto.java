package com.example.Personalized_Car_Recommendation_System.dto;


public class CarRecommendationDto {
    private Integer carId;
    private String brandName;
    private String fullName;
    private String priceRange;
    private Float avgScore;

    public CarRecommendationDto(Integer carId, String brandName, String fullName, String priceRange, Float avgScore) {
        this.carId = carId;
        this.brandName = brandName;
        this.fullName = fullName;
        this.priceRange = priceRange;
        this.avgScore = avgScore;
    }

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

    public Float getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Float avgScore) {
        this.avgScore = avgScore;
    }
}