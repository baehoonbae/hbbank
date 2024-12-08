package com.hbbank.backend.dto;

import java.math.BigDecimal;

import com.hbbank.backend.domain.Account;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountResponseDTO {
    private Long id;
    private String accountName;
    private String accountNumber;
    private BigDecimal balance;
    private Double interestRate;

    public static AccountResponseDTO from(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .accountName(account.getAccountType().getName())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .interestRate(account.getAccountType().getInterestRate())
                .build();
    }
}
