package com.nhnacademy.authapi.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String userId,
        @NotBlank
        String password
) {
}
