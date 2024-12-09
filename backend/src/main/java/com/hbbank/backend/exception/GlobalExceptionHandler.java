package com.hbbank.backend.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

// 전역 예외 처리 클래스
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        // 모든 validation 오류 메시지를 수집
        String errorMessage = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(error -> {
                // 필드 에러인 경우 필드명도 포함
                if (error instanceof FieldError) {
                    return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                }
                return error.getDefaultMessage();
            })
            .collect(Collectors.joining(", "));

        log.error("Validation failed: {}", errorMessage);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(errorMessage));
    }

    // 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred: ", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("서버 오류가 발생했습니다."));
    }
    
    // 토큰 관련 예외 처리
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        log.error("Token validation failed: ", e);
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(e.getMessage()));
    }
} 