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

    @NotNull(message = "이체일은 필수입니다")
    @Min(value = 1, message = "이체일은 1일 이상이어야 합니다")
    @Max(value = 31, message = "이체일은 31일 이하여야 합니다")
    private final int transferDay;

    @NotNull(message = "시작일은 필수입니다")
    private final LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private final LocalDate endDate;

    @NotBlank(message = "비밀번호는 필수입니다")
    private final String password;

    public void validate() {
        validateTransferDay();
        validateDateRange();
        validateAmount();
    }

    public void validateTransferDay() {
        if (transferDay < 1) {
            throw new IllegalArgumentException("이체일은 1일 이상이어야 합니다");
        }

        // 2월인 경우
        if (startDate.getMonthValue() == 2) {
            if (transferDay > 28) {
                throw new IllegalArgumentException("2월의 이체일은 28일 이하여야 합니다");
            }
        } // 4,6,9,11월인 경우 
        else if (startDate.getMonthValue() == 4
                || startDate.getMonthValue() == 6
                || startDate.getMonthValue() == 9
                || startDate.getMonthValue() == 11) {
            if (transferDay > 30) {
                throw new IllegalArgumentException("해당 월의 이체일은 30일 이하여야 합니다");
            }
        } // 1,3,5,7,8,10,12월인 경우
        else {
            if (transferDay > 31) {
                throw new IllegalArgumentException("이체일은 31일 이하여야 합니다");
            }
        }
    }

    public void validateDateRange() {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 앞설 수 없습니다.");
        }
    }

    public void validateAmount() {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("이체 금액은 0보다 커야 합니다.");
        }
    }
}
