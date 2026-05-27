package com.nhnacademy.authapi.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
