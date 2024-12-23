package com.hbbank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "이체 설명은 100자를 초과할 수 없습니다")
    private final String description;

    @NotNull(message = "예약 실행일시는 필수입니다")
    private final LocalDateTime reservedAt;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{4}$", message = "비밀번호는 4자리 숫자여야 합니다")
    private final String password;

    public void validate() {
        validateDateRange();
        validateReservedAt();
        validateAmount();
    }

    public void validateDateRange() {
        int day = reservedAt.getDayOfMonth();
        int month = reservedAt.getMonthValue();
        if (month == 2) {
            if (day > 28) {
                throw new IllegalArgumentException("2월의 이체일은 28일 이하여야 합니다");
            }
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            if (day > 30) {
                throw new IllegalArgumentException("해당 월의 이체일은 30일 이하여야 합니다");
            }
        } else {
            if (day > 31) {
                throw new IllegalArgumentException("이체일은 31일 이하여야 합니다");
            }
        }
    }

    public void validateReservedAt() {
        if (reservedAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("예약 실행일시는 현재보다 미래여야 합니다");
        }
    }

    public void validateAmount() {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("이체 금액은 0보다 커야 합니다.");
        }
    }
}
