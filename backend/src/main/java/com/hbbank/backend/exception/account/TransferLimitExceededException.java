package com.hbbank.backend.exception.account;

public class TransferLimitExceededException extends RuntimeException {

    public TransferLimitExceededException(){
        super("1회 이체한도를 초과했습니다.");
    }

    public TransferLimitExceededException(String message){
        super(message);
    }

}
