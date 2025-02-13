package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Integer id;
    @Column(name = "adminname")
    private String adminname;
    @Column(name = "password")
    private String password;
    @Column(nullable = false,name = "role")
    private String role;
    // getter 和 setter
    public Integer getId(){ return id; }
    public void setId(Integer id){ this.id = id; }
    public String getAdminname(){ return adminname; }
    public void setAdminname(String adminname){ this.adminname = adminname; }
    public String getPassword(){ return password; }
    public void setPassword(String password){ this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role != null ? role : "admin"; }
}