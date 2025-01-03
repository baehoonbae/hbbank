package com.hbbank.backend.exception.transaction;

public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException() {
        super("유효하지 않은 날짜 범위입니다.");
    }

    public InvalidDateRangeException(String msg) {
        super(msg);
    }
}
