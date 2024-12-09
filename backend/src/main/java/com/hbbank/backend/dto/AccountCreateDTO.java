package com.hbbank.backend.dto;

import java.math.BigDecimal;

import com.hbbank.backend.domain.Account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountCreateDTO {
    @NotNull(message = "사용자 ID는 필수입니다")
    private final Long userId;
    
    @NotBlank(message = "계좌 유형 코드는 필수입니다")
    private final String accountTypeCode;
    
    @NotNull(message = "초기 잔액은 필수입니다")
    @PositiveOrZero(message = "초기 잔액은 0 이상이어야 합니다")
    private final BigDecimal balance;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{4}$", message = "비밀번호는 4자리 숫자여야 합니다")
    private final String password;

    public static AccountCreateDTO from(Account account) {
        return AccountCreateDTO.builder()
                .userId(account.getUser().getId())
                .accountTypeCode(account.getAccountType().getCode())
                .balance(account.getBalance())
                .password(account.getPassword())
                .build();
    }

}