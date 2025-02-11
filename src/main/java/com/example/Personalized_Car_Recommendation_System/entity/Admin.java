package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String adminname;
    private String password;
    // getter 和 setter
    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }
    public String getAdminname(){ return adminname; }
    public void setAdminname(String adminname){ this.adminname = adminname; }
    public String getPassword(){ return password; }
    public void setPassword(String password){ this.password = password; }
}