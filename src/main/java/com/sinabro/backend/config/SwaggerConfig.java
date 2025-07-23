package com.sinabro.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🐼 시나브로 API 명세서")
                        .version("1.0.0")
                        .description("다문화 가정 아동을 위한 한국어 학습 플랫폼 시나브로의 백엔드 API 문서입니다."));
    }
}
