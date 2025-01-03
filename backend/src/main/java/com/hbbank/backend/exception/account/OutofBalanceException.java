package com.hbbank.backend.exception.account;

public class OutofBalanceException extends RuntimeException {
    
    public OutofBalanceException(){
        super("잔액이 부족합니다.");
    }

    public OutofBalanceException(String message){
        super(message);
    }
}
