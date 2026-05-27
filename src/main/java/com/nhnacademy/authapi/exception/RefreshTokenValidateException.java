package com.nhnacademy.authapi.exception;

public class RefreshTokenValidateException extends RuntimeException {
    public RefreshTokenValidateException(String message) {
        super(message);
    }
}
