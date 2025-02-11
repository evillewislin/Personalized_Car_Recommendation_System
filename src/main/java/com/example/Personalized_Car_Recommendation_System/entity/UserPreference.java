package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_preference")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String preferredType;
    private String preferredFuel;
    // getter 和 setter
    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }
    public Integer getUserId(){ return userId; }
    public void setUserId(Integer userId){ this.userId = userId; }
    public BigDecimal getMinPrice(){ return minPrice; }
    public void setMinPrice(BigDecimal minPrice){ this.minPrice = minPrice; }
    public BigDecimal getMaxPrice(){ return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice){ this.maxPrice = maxPrice; }
    public String getPreferredType(){ return preferredType; }
    public void setPreferredType(String preferredType){ this.preferredType = preferredType; }
    public String getPreferredFuel(){ return preferredFuel; }
    public void setPreferredFuel(String preferredFuel){ this.preferredFuel = preferredFuel; }
}