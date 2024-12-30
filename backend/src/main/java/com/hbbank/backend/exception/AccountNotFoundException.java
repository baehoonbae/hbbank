package com.hbbank.backend.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("존재하지 않는 계좌입니다.");
    }

    public AccountNotFoundException(String msg) {
        super(msg);
    }

}
