package com.hbbank.backend.exception.user;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException() {
        super("사용자가 이미 존재합니다.");
    }

    public DuplicateUserException(String msg) {
        super(msg);
    }

}
