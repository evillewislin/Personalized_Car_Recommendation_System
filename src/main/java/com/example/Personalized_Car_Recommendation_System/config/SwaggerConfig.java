package com.example.Personalized_Car_Recommendation_System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("个性化汽车推荐系统 API") // API 标题
                        .version("1.0.0")             // 版本号
                        .description("系统接口文档"));  // 描述
    }
}
