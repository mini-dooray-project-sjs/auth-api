package com.nhnacademy.authapi.service;

import com.nhnacademy.authapi.dto.LoginRequest;
import com.nhnacademy.authapi.dto.TokenResponse;

public interface AuthService {
    TokenResponse login(
            LoginRequest req
    );

    void logout(String accessToken);

    TokenResponse refresh(String refreshToken);
}
