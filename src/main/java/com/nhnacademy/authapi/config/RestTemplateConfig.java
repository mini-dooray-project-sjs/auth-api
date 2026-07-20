package com.nhnacademy.authapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // RestTemplate 빈을 생성하여 외부 API 호출에 사용할 수 있도록 설정
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
