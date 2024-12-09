package com.hbbank.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferRequestDTO {

    @NotNull(message = "출금 계좌 ID는 필수입니다")
    private final Long fromAccountId; // 출금 계좌 id
    
    @NotBlank(message = "입금 계좌번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{14}$", message = "입금 계좌번호는 14자리 숫자여야 합니다")
    private final String toAccountNumber; // 입금계좌 계좌번호

    @NotNull(message = "이체 금액은 필수입니다")
    @Positive(message = "이체 금액은 0보다 커야 합니다")
    private final BigDecimal amount; // 이체 금액

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{4}$", message = "비밀번호는 4자리 숫자여야 합니다")
    private final String password; // 출금 계좌 비밀번호
}
