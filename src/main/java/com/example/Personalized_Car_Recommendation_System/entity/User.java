package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    // getter 和 setter
    public Integer getId() { return id; }
    public void setId(Integer id){ this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username){ this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password){ this.password = password; }
}