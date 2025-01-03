package com.hbbank.backend.exception;

import java.util.stream.Collectors;

import com.hbbank.backend.exception.account.*;
import com.hbbank.backend.exception.autoTransfer.AutoTransferNotFoundException;
import com.hbbank.backend.exception.autoTransfer.InvalidAutoTransferPasswordException;
import com.hbbank.backend.exception.reserveTransfer.InvalidReserveTransferPasswordException;
import com.hbbank.backend.exception.reserveTransfer.ReserveTransferNotFoundException;
import com.hbbank.backend.exception.token.InvalidTokenException;
import com.hbbank.backend.exception.user.DuplicateUserException;
import com.hbbank.backend.exception.user.InvalidUserPasswordException;
import com.hbbank.backend.exception.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
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
        log.error("서버 오류 발생: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

    // 토큰 관련 예외 처리
    @ExceptionHandler({
            InvalidTokenException.class,
            InvalidBearerTokenException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        log.error("토큰 검증 실패: ", e);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
    }

    /* 사용자 관련 예외 처리 */
    @ExceptionHandler({
            DuplicateUserException.class,
            InvalidUserPasswordException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleUserException(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e instanceof DuplicateUserException) {
            status = HttpStatus.CONFLICT;
        } else if (e instanceof InvalidUserPasswordException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (e instanceof UserNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        log.error("사용자 관련 오류 발생: ", e);
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getMessage()));
    }

    /* 계좌 관련 예외 처리 */
    @ExceptionHandler({
            AccountNotFoundException.class,
            AccountTypeNotFoundException.class,
            DailyTransferLimitExceededException.class,
            InvalidAccountPasswordException.class,
            InvalidAccountStatusException.class,
            OutofBalanceException.class,
            TransferLimitExceededException.class
    })
    public ResponseEntity<ErrorResponse> handleAccountException(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e instanceof AccountNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (e instanceof InvalidAccountPasswordException) {
            status = HttpStatus.UNAUTHORIZED;
        }
        log.error("계좌 관련 오류 발생: ", e);
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getMessage()));
    }

    /* 자동이체 관련 예외 처리 */
    @ExceptionHandler({
            AutoTransferNotFoundException.class,
            InvalidAutoTransferPasswordException.class
    })
    public ResponseEntity<ErrorResponse> handleAutoTransferException(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e instanceof AutoTransferNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (e instanceof InvalidAutoTransferPasswordException) {
            status = HttpStatus.UNAUTHORIZED;
        }
        log.error("계좌 관련 오류 발생: ", e);
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getMessage()));
    }

    /* 예약이체 관련 예외 처리 */
    @ExceptionHandler({
            ReserveTransferNotFoundException.class,
            InvalidReserveTransferPasswordException.class
    })
    public ResponseEntity<ErrorResponse> handleReserveTransferException(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e instanceof ReserveTransferNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (e instanceof InvalidReserveTransferPasswordException) {
            status = HttpStatus.UNAUTHORIZED;
        }
        log.error("계좌 관련 오류 발생: ", e);
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getMessage()));
    }

}