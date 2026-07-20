package com.nhnacademy.authapi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * 토큰 생성, 검증, 파싱을 담당하는 클래스
 */
@Slf4j
@Component
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    @Autowired
    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes= Decoders.BASE64.decode(jwtProperties.getSecretKey());
        this.secretKey= Keys.hmacShaKeyFor(keyBytes);
    }

    // access token 생성
    public String createAccessToken(String userId, String role) {
        long now=System.currentTimeMillis();
        Date accessTokenExpiration = new Date(now + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(userId)                    // 토큰의 주체 (userId)
                .claim("role", role)             // 토큰에 추가적인 정보(클레임)로 역할(role) 저장
                .issuedAt(new Date(now))            // 토큰 발행 시간
                .expiration(accessTokenExpiration)  // 토큰 만료 시간
                .signWith(secretKey)                // 토큰 서명에 사용할 비밀 키 설정
                .compact();                         // 토큰 생성 및 직렬화하여 문자열로 반환
    }

    // refresh token 생성
    public String createRefreshToken(String userId) {
        long now=System.currentTimeMillis();
        Date refreshTokenExpiration = new Date(now + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(userId)                    // 토큰의 주체 (userId)
                .issuedAt(new Date(now))            // 토큰 발행 시간
                .expiration(refreshTokenExpiration) // 토큰 만료 시간
                .signWith(secretKey)                // 토큰 서명에 사용할 비밀 키 설정
                .compact();                         // 토큰 생성 및 직렬화하여 문자열로 반환
    }

    // 토큰 검증 -> 유효한 토큰인지, 만료되었는지 검사
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true; // 토큰이 유효하면 true 반환
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false; // 토큰이 유효하지 않으면 false 반환
        }
    }

    // 토큰에서 userId 추출
    public String getUserIdFromToken(String token) {
        return getClaims(token).getSubject(); // 토큰의 주체(subject)에서 userId 추출
    }

    // 토큰에서 role 추출
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class); // 토큰의 클레임에서 role 추출
    }

    // 토큰에서 남은 유효 기간 계산
    public Long getRemainingTime(String token) {
        try {
            Date expiration=getClaims(token).getExpiration(); // 토큰의 만료 시간 추출
            long now=new Date().getTime();
            return expiration.getTime()-now; // 남은 유효 기간 계산 (만료 시간 - 현재 시간)
        } catch(JwtException e) {
            return 0L; // 토큰이 유효하지 않으면 남은 시간 0 반환
        }
    }

    // 토큰에서 클레임(정보) 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)      // 토큰 서명 검증에 사용할 비밀 키 설정
                .build()                    // JWT 파서 빌드
                .parseSignedClaims(token)   // 파싱 시도 - 서명된 클레임을 파싱
                .getPayload();              // 파싱 성공 시, 클레임(토큰의 내용) 반환
    }
}
