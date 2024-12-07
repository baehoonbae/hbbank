package com.hbbank.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTokenException() {
        super("유효하지 않은 토큰입니다.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTokenException(Throwable cause) {
        super("유효하지 않은 토큰입니다.", cause);
    }
}
