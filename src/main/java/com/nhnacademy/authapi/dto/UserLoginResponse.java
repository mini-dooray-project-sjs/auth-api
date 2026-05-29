package com.nhnacademy.authapi.dto;

import com.nhnacademy.authapi.entity.UserRole;
import com.nhnacademy.authapi.entity.UserStatus;
import lombok.Builder;

@Builder
public record UserLoginResponse(
        String id,
        String password,
        UserStatus status,
        UserRole role
){
}
