package com.example.Personalized_Car_Recommendation_System.service.impl;


import com.example.Personalized_Car_Recommendation_System.service.RecommendationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    @Override
    public int getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        System.out.println(SECRET_KEY);
        return claims.get("user_id", Integer.class);
    }
}
