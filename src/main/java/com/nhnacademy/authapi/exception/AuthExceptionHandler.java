package com.nhnacademy.authapi.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 인증 관련 예외를 처리하는 글로벌 예외 처리 클래스입니다.
 * 인증 실패, 권한 부족 등의 예외를 잡아서 적절한 HTTP 응답으로 변환합니다.
 */
@RestControllerAdvice
public class AuthExceptionHandler {
}
