package com.example.Personalized_Car_Recommendation_System.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private static String secret;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtil.secret = secret;
    }

    private static final long EXPIRATION_TIME = 86400000; // 1天

    private static Key getSigningKey() {
        if (secret == null) {
            logger.error("JWT 密钥未配置，请检查配置文件");
            throw new IllegalArgumentException("JWT 密钥未配置，请检查配置文件");
        }
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateToken(Integer userId) {
        Key signingKey = getSigningKey();
        return Jwts.builder()
                .setSubject(userId.toString())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Integer getUserIdFromToken(String token) {
        try {
            Key signingKey = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Integer.parseInt(claims.getSubject());
        } catch (ExpiredJwtException e) {
            logger.error("Token 已过期", e);
            throw new IllegalArgumentException("Token 已过期，请重新登录");
        } catch (SignatureException | IllegalArgumentException | MalformedJwtException e) {
            logger.error("Token 解析失败", e);
            throw new IllegalArgumentException("Token 解析失败");
        }
    }
}