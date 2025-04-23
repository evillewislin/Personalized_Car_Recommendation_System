package com.example.Personalized_Car_Recommendation_System.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "car_info")
public class CarInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Integer id;

    @Column(name = "brand_id")
    private Integer brandId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "minprice")
    private Integer minPrice;

    @Column(name = "maxprice")
    private Integer maxPrice;

    @Column(name = "energy_index")
    private Integer energy_index;

    @Column(name = "safety_index")
    private Integer safety_index;

    @Column(name = "Intelligence_index")
    private Integer Intelligence_index;

    @Column(name = "comfort_index")
    private Integer comfort_index;

    @Column(name = "appearance_index")
    private Integer appearance_index;

    @Column(name = "space_index")
    private Integer space_index;

}
