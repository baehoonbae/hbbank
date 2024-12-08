package com.hbbank.backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreateDTO {
    private Long userId;           // 유저 ID만 받음
    private String accountTypeCode;  // 계좌 타입 코드만 받음
    private BigDecimal balance;    // 초기 잔액
    private String password;       // 비밀번호
}