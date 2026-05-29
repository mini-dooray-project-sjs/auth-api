package com.nhnacademy.authapi.entity;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class User {

    private String id;
    private String password;
    private UserStatus status;
    private UserRole role;
}
