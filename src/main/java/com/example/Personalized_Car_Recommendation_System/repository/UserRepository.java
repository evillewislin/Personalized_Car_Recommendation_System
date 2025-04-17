package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;


public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

    User findByUserId(Integer userId);


    // 查找符合地区且年龄范围的用户ID列表
    @Query("SELECT u.userId FROM User u WHERE u.region = :region AND u.age BETWEEN :minAge AND :maxAge")
    List<Long> findUserIdsByRegionAndAgeBetween(
            @Param("region") String region,
            @Param("minAge") int minAge,
            @Param("maxAge") int maxAge);

    @Query("SELECT u.userId FROM User u WHERE u.age BETWEEN :minAge AND :maxAge")
    List<Long> findUserIdsByAgeBetween(
            @Param("minAge") int minAge,
            @Param("maxAge") int maxAge);
}
