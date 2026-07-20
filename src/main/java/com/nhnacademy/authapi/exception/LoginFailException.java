package com.nhnacademy.authapi.exception;

public class LoginFailException extends RuntimeException {
    public LoginFailException(String message) {
        super(message);
    }
}
