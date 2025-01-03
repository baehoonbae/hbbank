package com.hbbank.backend.exception.autoTransfer;

public class InvalidAutoTransferPasswordException extends RuntimeException {

    public InvalidAutoTransferPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }

    public InvalidAutoTransferPasswordException(String message) {
        super(message);
    }
}
