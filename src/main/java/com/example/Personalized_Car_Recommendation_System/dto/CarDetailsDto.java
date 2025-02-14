package com.example.Personalized_Car_Recommendation_System.dto;

public class CarDetailsDto {
    private String name;
    private String fullName;
    private Integer minPrice;
    private Integer maxPrice;
    private String img;

    // 构造函数
    public CarDetailsDto(String name, String fullName, Integer minPrice, Integer maxPrice, String img) {
        this.name = name;
        this.fullName = fullName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.img = img;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
}