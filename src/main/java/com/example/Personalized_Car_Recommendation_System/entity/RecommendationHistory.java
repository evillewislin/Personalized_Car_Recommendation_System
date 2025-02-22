package com.example.Personalized_Car_Recommendation_System.entity;


import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "recommendation_history")
public class RecommendationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "car_id")
    private Integer carId;
    @Column(name = "score")
    private Float score;
    @Column(name = "name")
    private String name;
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    // getter 和 setter
    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }
    public Integer getUserId(){ return userId; }
    public void setUserId(Integer user_Id){ this.userId = user_Id; }
    public Integer getCarId(){ return carId; }
    public void setCarId(Integer carId){ this.carId = carId; }
    public String getCarName(){ return name; }
    public void setCarName(String name){ this.name = name; }
    public Float getScore(){ return score; }
    public void setScore(Float score){ this.score = score; }
    public Date getTimestamp(){ return timestamp; }
    public void setTimestamp(Date timestamp){ this.timestamp = timestamp; }

}
