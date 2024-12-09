package com.hbbank.backend.dto;

import java.math.BigDecimal;

import com.hbbank.backend.domain.Account;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountResponseDTO {
    private final Long id;
    private final String accountName;
    private final String accountNumber;
    private final BigDecimal balance;
    private final Double interestRate;

    public static AccountResponseDTO from(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .accountName(account.getAccountType().getName())
                .accountNumber(formatAccountNumber(account.getAccountNumber()))
                .balance(account.getBalance())
                .interestRate(account.getAccountType().getInterestRate())
                .build();
    }

    private static String formatAccountNumber(String number) {
        return String.format("%s-%s-%s-%s-%s",
            number.substring(0, 3),
            number.substring(3, 6),
            number.substring(6, 8),
            number.substring(8, 14),
            number.substring(14));
    }
}
