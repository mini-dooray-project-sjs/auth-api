package com.nhnacademy.authapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * application.yml에서 jwt 관련 설정을 읽어오는 클래스입니다.
 */
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix="jwt")
public class JwtProperties {
    private String secretKey;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}
