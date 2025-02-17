package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userid;

    @Column(unique = true, nullable = false,name = "username")
    private String username;

    @Column(nullable = false,name = "password")
    private String password;

    @Column(nullable = false,name = "role")
    private String role;

    // getter 和 setter
    public Integer getId() { return userid; }
    public void setId(Integer userid) { this.userid = userid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role != null ? role : "user"; }

    public void setOldPassword(String oldPassword) {this.password = oldPassword;
    }

    public void setNewPassword(String newPassword) {this.password =newPassword;
    }
}
