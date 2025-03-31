package com.example.Personalized_Car_Recommendation_System.dto;

public class ImCarDetailsDto {
    private Integer carId;
    private Integer userId;
    private String name;
    private String fullName;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer score;
    private Integer interactions;
    



    // 构造函数
    public ImCarDetailsDto(Integer carId,Integer userId,String name, String fullName, Integer minPrice, Integer maxPrice,Integer score,Integer interactions)     {
        this.carId = carId;
        this.userId = userId;
        this.name = name;
        this.fullName = fullName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.score = score;
        this.interactions = 0; // 默认值为0

    }

    // Getters and Setters
    public Integer getCarId() { return carId; }
    public void setCarId(Integer carId) { this.carId = carId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getInteractions() { return interactions; }
    public void setInteractions(Integer interactions) { this.interactions = interactions; }
}