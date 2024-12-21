package com.hbbank.backend.exception;

public class DailyTransferLimitExceededException extends RuntimeException {

    public DailyTransferLimitExceededException() {
        super("잔액이 부족합니다.");
    }

    public DailyTransferLimitExceededException(String message) {
        super(message);
    }

}
