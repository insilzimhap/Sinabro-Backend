package com.sinabro.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// 이 클래스가 스프링의 설정 파일임을 알려주는 어노테이션
@Configuration
public class RestTemplateConfig {

    // @Bean 어노테이션을 붙여주면, 이 메서드가 반환하는 객체를
    // 스프링 컨테이너(IoC Container)가 관리하는 Bean으로 등록해줘.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}