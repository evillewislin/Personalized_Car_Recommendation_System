package com.example.Personalized_Car_Recommendation_System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // 关闭 CSRF 保护
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/**","/api/auth/**","/ai/recommend").permitAll()
                        .anyRequest().authenticated() // 其他所有请求都需要认证
                );

        return http.build();
    }
}
