package com.hbbank.backend.exception.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidAccountPasswordException extends RuntimeException {

    public InvalidAccountPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    public InvalidAccountPasswordException(String message) {
        super(message);
    }
}
