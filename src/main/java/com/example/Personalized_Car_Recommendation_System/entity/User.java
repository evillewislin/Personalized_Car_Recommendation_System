package com.example.Personalized_Car_Recommendation_System.entity;

import jakarta.persistence.*;

/**
 * 用户实体类，对应数据库中的 user 表
 */
@Entity
@Table(name = "user")
public class User {
    /**
     * 用户 ID，主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 用户名，唯一且不能为空
     */
    @Column(unique = true, nullable = false, name = "username")
    private String username;

    /**
     * 用户密码，不能为空
     */
    @Column(nullable = false, name = "password")
    private String password;

    /**
     * 用户角色，不能为空，默认为 "user"
     */
    @Column(nullable = false, name = "role")
    private String role;

    /**
     * 获取用户 ID
     * @return 用户 ID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置用户 ID
     * @param userId 用户 ID
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户密码
     * @return 用户密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置用户密码
     * @param password 用户密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取用户角色
     * @return 用户角色
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置用户角色，若传入 null 则默认为 "user"
     * @param role 用户角色
     */
    public void setRole(String role) {
        this.role = role != null ? role : "user";
    }
}