package com.example.Personalized_Car_Recommendation_System.dto;

public class CarDetailsDto {
    private Integer carId;
    private String name;
    private String fullName;
    private Integer minPrice;
    private Integer maxPrice;



    // 构造函数
    public CarDetailsDto(Integer carId,String name, String fullName, Integer minPrice, Integer maxPrice)     {
        this.carId = carId;
        this.name = name;
        this.fullName = fullName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;

    }

    // Getters and Setters
    public Integer getCarId() { return carId; }
    public void setCarId(Integer carId) { this.carId = carId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }
}