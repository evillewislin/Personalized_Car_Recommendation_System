package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "car_brand")
public class CarBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "img")
    private String img;


    // getter 和 setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

}
