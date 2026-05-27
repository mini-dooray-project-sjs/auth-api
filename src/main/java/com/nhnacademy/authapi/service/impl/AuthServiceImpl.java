package com.nhnacademy.authapi.service.impl;

import com.nhnacademy.authapi.config.JwtProperties;
import com.nhnacademy.authapi.config.JwtProvider;
import com.nhnacademy.authapi.dto.LoginRequest;
import com.nhnacademy.authapi.dto.TokenResponse;
import com.nhnacademy.authapi.entity.User;
import com.nhnacademy.authapi.entity.UserStatus;
import com.nhnacademy.authapi.exception.LoginFailException;
import com.nhnacademy.authapi.exception.RefreshTokenValidateException;
import com.nhnacademy.authapi.repository.AuthRepository;
import com.nhnacademy.authapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 비밀번호 검증, 토큰 발급 오케스트레이션 등 인증 관련 비즈니스 로직을 담당하는 서비스 클래스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder encoder;
    private final AuthRepository repository;
    private final RedisTemplate<String, String> redisTemplate;


    // 로그인
    @Override
    public TokenResponse login(
            LoginRequest req
    ) {
        // 로그인 요청에서 아이디와 비밀번호 추출
        String userId=req.userId();
        String password=req.password();

        // 아이디로 유저 조회
        User user=repository.findByById(userId);

        // 로그인 실패 조건: 유저가 존재하지 않거나, 비밀번호가 일치하지 않거나, 계정이 활성화되어 있지 않은 경우
        if(user==null
                || !encoder.matches(password, user.getPassword())
                || !Objects.equals(user.getStatus(), UserStatus.ACTIVE)
        ) {
            throw new LoginFailException("아이디 또는 비밀번호가 일치하지 않거나, 계정이 활성화되어 있지 않습니다.");
        }

        String role="ROLE_"+user.getRole().toString();

        // 로그인 성공 시, JWT 액세스 토큰과 리프레시 토큰 발급
        String accessToken=jwtProvider.createAccessToken(userId, role);
        String refreshToken=jwtProvider.createRefreshToken(userId);

        // 발급된 리프레시 토큰을 Redis에 저장 (키: "refreshToken:{userId}", 값: refreshToken)
        String redisKey="refreshToken:"+userId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, jwtProperties.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        return new TokenResponse(accessToken, refreshToken);
    }

    // 로그아웃
    @Override
    public void logout(String accessToken) {
        // 액세스 토큰에서 유저 아이디 추출
        String userId=jwtProvider.getUserIdFromToken(accessToken);

        // Redis에서 해당 유저의 리프레시 토큰 삭제
        String redisKey="refreshToken:"+userId;
        redisTemplate.delete(redisKey);

        // 블랙리스트에 엑세스 토큰 저장 (TTL: 액세스 토큰의 남은 유효 기간)
        long remainingMillisSeconds=jwtProvider.getRemainingTime(accessToken);

        // 남은 유효 기간이 0보다 큰 경우에만 블랙리스트에 등록 (이미 만료된 토큰은 블랙리스트에 등록할 필요 없음)
        if(remainingMillisSeconds>0) {
            String key="blacklist:"+accessToken;
            redisTemplate.opsForValue().set(key, "logout", remainingMillisSeconds, TimeUnit.MILLISECONDS);
        }
        log.info("유저 {} 로그아웃 처리 완료. 액세스 토큰 블랙리스트 등록: {}, 남은 유효 기간: {}ms", userId, accessToken, remainingMillisSeconds);
    }

    // 토큰 재발급
    @Override
    public TokenResponse refresh(String refreshToken) {
        // 리프레시 토큰 검증: 유효한 토큰인지 + Redis에 저장된 토큰과 일치하는지
        if(!jwtProvider.validateToken(refreshToken)) {
            throw new RefreshTokenValidateException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰에서 유저 아이디 추출
        String userId=jwtProvider.getUserIdFromToken(refreshToken);

        // Redis에서 해당 유저의 리프레시 토큰 조회
        String redisKey="refreshToken:"+userId;
        String storedRefreshToken=redisTemplate.opsForValue().get(redisKey);

        // Redis에 저장된 토큰과 요청에서 전달된 토큰이 일치하는지 확인
        if(storedRefreshToken==null||!storedRefreshToken.equals(refreshToken)) {
            throw new RefreshTokenValidateException("리프레시 토큰이 일치하지 않거나, 이미 만료되었습니다.");
        }

        // 유저 아이디로 유저 조회 -> 토큰 재발급 시점에 유저의 권한이 변경되었을 수 있으므로, 최신 정보를 조회하여 토큰에 반영
        User user=repository.findByById(userId);
        String role="ROLE_"+user.getRole().toString();

        // 새로운 액세스 토큰과 리프레시 토큰 발급
        String newAccessToken=jwtProvider.createAccessToken(userId, role);
        String newRefreshToken=jwtProvider.createRefreshToken(userId);

        // Redis에 새로운 리프레시 토큰 저장 (기존 토큰 덮어쓰기)
        redisTemplate.opsForValue().set(redisKey, newRefreshToken, jwtProperties.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        log.info("리프레시 토큰 재발급 성공. 유저: {}, 새 액세스 토큰: {}, 새 리프레시 토큰: {}", userId, newAccessToken, newRefreshToken);

        // 새로운 토큰을 담은 응답 반환
        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
