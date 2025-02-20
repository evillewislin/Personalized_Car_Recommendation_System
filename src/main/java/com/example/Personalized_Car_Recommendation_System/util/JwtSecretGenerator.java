package com.example.Personalized_Car_Recommendation_System.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;

public class JwtSecretGenerator {
    public static void main(String[] args) {
        // 生成一个适合 HS256 算法的密钥
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // 将密钥进行 Base64 编码
        String base64EncodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("Base64 编码后的密钥: " + base64EncodedKey);
    }
}