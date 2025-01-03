package com.hbbank.backend.exception.account;

public class AccountTypeNotFoundException extends RuntimeException {

    public AccountTypeNotFoundException() {
        super("존재하지 않는 계좌 타입입니다.");
    }

    public AccountTypeNotFoundException(String msg) {
        super(msg);
    }

}
