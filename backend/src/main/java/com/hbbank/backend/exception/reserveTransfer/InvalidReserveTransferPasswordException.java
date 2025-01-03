package com.hbbank.backend.exception.reserveTransfer;

public class InvalidReserveTransferPasswordException extends RuntimeException {

    public InvalidReserveTransferPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    public InvalidReserveTransferPasswordException(String message) {
        super(message);
    }
}
