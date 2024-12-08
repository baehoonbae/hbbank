package com.hbbank.backend.dto;

import java.math.BigDecimal;

import com.hbbank.backend.domain.Account;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountCreateDTO {
    private final Long userId;           // 유저 ID만 받음
    private final String accountTypeCode;  // 계좌 타입 코드만 받음
    private final BigDecimal balance;    // 초기 잔액
    private final String password;       // 비밀번호

    public static AccountCreateDTO from(Account account) {
        return AccountCreateDTO.builder()
                .userId(account.getUser().getId())
                .accountTypeCode(account.getAccountType().getCode())
                .balance(account.getBalance())
                .password(account.getPassword())
                .build();
    }

}