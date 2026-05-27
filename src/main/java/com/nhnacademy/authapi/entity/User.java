package com.nhnacademy.authapi.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter @Builder
public class User {

    private String id;
    private String password;
    private UserStatus status;
    private UserRole role;
//    private String email;
//    private ZonedDateTime createdAt;
}
