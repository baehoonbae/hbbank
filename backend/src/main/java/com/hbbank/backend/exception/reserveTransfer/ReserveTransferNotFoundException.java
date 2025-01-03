package com.hbbank.backend.exception.reserveTransfer;

public class ReserveTransferNotFoundException extends RuntimeException {

    public ReserveTransferNotFoundException() {
        super("존재하지 않는 자동이체입니다.");
    }

    public ReserveTransferNotFoundException(String msg) {
        super(msg);
    }
}
