package com.example.Personalized_Car_Recommendation_System.dto;

public class ImCarDetailsDto {
    private Integer carId;
    private String brandName;
    private String fullName;
    private Integer minPrice;
    private Integer maxPrice;
    private Double predictedRating;

    public ImCarDetailsDto(Integer carId, String brandName, String fullName, Integer minPrice, Integer maxPrice, Double predictedRating) {
        this.carId = carId;
        this.brandName = brandName;
        this.fullName = fullName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.predictedRating = predictedRating;
    }

    @Override
    public String toString() {
        return "ImCarDetailsDto{" +
                "carId=" + carId +
                ", brandName='" + brandName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", predictedRating=" + predictedRating +
                '}';
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

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getPredictedRating() {
        return predictedRating;
    }

    public void setPredictedRating(Double predictedRating) {
        this.predictedRating = predictedRating;
    }
}