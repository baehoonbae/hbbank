package com.hbbank.backend.exception.autoTransfer;

public class AutoTransferNotFoundException extends RuntimeException {

    public AutoTransferNotFoundException() {
        super("존재하지 않는 자동이체입니다.");
    }

    public AutoTransferNotFoundException(String msg) {
        super(msg);
    }
}
