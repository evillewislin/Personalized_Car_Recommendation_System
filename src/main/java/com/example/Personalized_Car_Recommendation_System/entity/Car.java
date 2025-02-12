package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String brand;
    private Double price;

    private String score;
    @Column(name = "image_url")
    private String imageUrl;
    // getter 和 setter
    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }
    public String getBrand(){ return brand; }
    public void setBrand(String brand){ this.brand = brand; }
    public Double getPrice(){ return price; }
    public void setPrice(Double price){ this.price = price; }
    public String getScore(){ return score; }
    public void setScore(String score){ this.score = score; }
    public String getImageUrl(){ return imageUrl; }
    public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }
}