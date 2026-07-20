package com.nhnacademy.authapi.entity;

public enum UserStatus {
    ACTIVE(0),
    DORMANT(1),
    DELETED(2);

    private final int code;

    UserStatus(int code) {
        this.code=code;
    }

    public static UserStatus fromCode(int code) {
        for(UserStatus u: UserStatus.values()) {
            if(u.code==code) {
                return u;
            }
        }
        throw new IllegalArgumentException("Invalid code for UserStatus: " + code);
    }
}
