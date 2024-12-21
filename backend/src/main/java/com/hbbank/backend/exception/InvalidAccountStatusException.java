package com.hbbank.backend.exception;

public class InvalidAccountStatusException extends RuntimeException {

    public InvalidAccountStatusException(){
        super("유효하지 않은 계좌 상태입니다.");
    }

    public InvalidAccountStatusException(String message){
        super(message);
    }

}
