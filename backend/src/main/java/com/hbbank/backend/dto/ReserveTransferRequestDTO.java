package com.hbbank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReserveTransferRequestDTO {

    @NotNull(message = "사용자 ID는 필수입니다")
    private final Long userId;

    @NotNull(message = "출금 계좌는 필수입니다")
    private final Long fromAccountId;

    @NotBlank(message = "입금 계좌번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{15}$", message = "입금 계좌번호는 15자리 숫자여야 합니다")
    private final String toAccountNumber;

    @NotNull(message = "이체 금액은 필수입니다")
    @Positive(message = "이체 금액은 0보다 커야 합니다")
    private final BigDecimal amount;

    @NotBlank(message = "이체 설명은 필수입니다")
    private final String description;

    @NotNull(message = "예약 실행일시는 필수입니다")
    private final LocalDateTime reservedAt;

    @NotBlank(message = "비밀번호는 필수입니다")
    private final String password;
}
