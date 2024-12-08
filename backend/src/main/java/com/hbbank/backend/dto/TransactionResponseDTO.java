package com.hbbank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionResponseDTO {
    private final Long id;
    private final Long accountId;
    private final LocalDateTime transactionDateTime; // 거래일시
    private final String transactionType; // 거래유형(입금/출금)
    private final String sender; // 보낸분
    private final String receiver; // 받는분
    private final BigDecimal withdrawalAmount; // 출금액(원)
    private final BigDecimal depositAmount; // 입금액(원)
    private final BigDecimal balance; // 잔액(원)

    public static TransactionResponseDTO from(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .transactionDateTime(transaction.getTransactionDateTime())
                .transactionType(transaction.getTransactionType())
                .sender(transaction.getSender())
                .receiver(transaction.getReceiver())
                .withdrawalAmount(transaction.getWithdrawalAmount())
                .depositAmount(transaction.getDepositAmount())
                .balance(transaction.getBalance())
                .build();
    }
}
