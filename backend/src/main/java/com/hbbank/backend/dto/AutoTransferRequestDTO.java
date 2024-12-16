package com.hbbank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AutoTransferRequestDTO {

    @NotNull(message = "출금 계좌는 필수입니다")
    private Long fromAccountId;

    @NotBlank(message = "입금 계좌번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{15}$", message = "입금 계좌번호는 15자리 숫자여야 합니다")
    private String toAccountNumber;

    @NotNull(message = "이체 금액은 필수입니다")
    @Positive(message = "이체 금액은 0보다 커야 합니다")
    private BigDecimal amount;

    @NotBlank(message = "이체 설명은 필수입니다")
    private String description;

    @NotNull(message = "이체일은 필수입니다")
    @Min(value = 1, message = "이체일은 1일 이상이어야 합니다")
    @Max(value = 31, message = "이체일은 31일 이하여야 합니다")
    private int transferDay;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
