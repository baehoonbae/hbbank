package com.hbbank.backend.exception.email;

public class InvalidVerificationCodeException extends RuntimeException {

    public InvalidVerificationCodeException() {
        super("인증 코드가 일치하지 않습니다.");
    }

    public InvalidVerificationCodeException(String msg) {
        super(msg);
    }
}
