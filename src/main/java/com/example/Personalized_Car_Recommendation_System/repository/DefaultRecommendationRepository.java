package com.example.Personalized_Car_Recommendation_System.repository;

import com.example.Personalized_Car_Recommendation_System.entity.DefaultRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DefaultRecommendationRepository extends JpaRepository<DefaultRecommendation, Integer> {
}