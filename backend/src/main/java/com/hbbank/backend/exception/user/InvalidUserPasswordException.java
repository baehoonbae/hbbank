package com.hbbank.backend.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidUserPasswordException extends RuntimeException {

    public InvalidUserPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    public InvalidUserPasswordException(String message) {
        super(message);
    }
}
