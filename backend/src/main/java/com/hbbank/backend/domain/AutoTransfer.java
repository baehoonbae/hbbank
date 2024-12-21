package com.hbbank.backend.domain;

import java.time.LocalDate;

import com.hbbank.backend.domain.enums.TransferStatus;
import com.hbbank.backend.dto.AutoTransferRequestDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Table(name = "auto_transfer")
public class AutoTransfer extends Transfer {

    @NotNull(message = "이체일은 필수입니다")
    @Min(value = 1, message = "이체일은 1일 이상이어야 합니다")
    @Max(value = 31, message = "이체일은 31일 이하여야 합니다")
    private int transferDay;                          // 매월 자동이체 실행일 (1-31) (사용자 입력)

    @NotNull(message = "다음 이체일은 필수입니다")
    private LocalDate nextTransferDate;           // 다음 자동이체 실행 예정일

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;                  // 자동이체 시작일 (사용자 입력)

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;                    // 자동이체 종료일 (사용자 입력)

    public void update(Account fromAccount, AutoTransferRequestDTO dto) {
        this.user = fromAccount.getUser();
        this.fromAccount = fromAccount;
        this.toAccountNumber = dto.getToAccountNumber();
        this.amount = dto.getAmount();
        this.description = dto.getDescription();
        this.status = TransferStatus.ACTIVE;

        this.transferDay = dto.getTransferDay();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.nextTransferDate = dto.getStartDate().withDayOfMonth(dto.getTransferDay());
    }

    public void updateNextTransferDate() {            // 다음 자동이체 실행일을 한 달 뒤로 업데이트
        this.nextTransferDate = this.nextTransferDate.plusMonths(1)
                .withDayOfMonth(this.transferDay);
    }

    public boolean isExpired() {                      // 자동이체 만료 여부 확인
        return endDate != null && LocalDate.now().isAfter(endDate);
    }

    @Override
    public boolean isExecutable() {
        return status == TransferStatus.ACTIVE
                && !isExpired()
                && LocalDate.now().equals(nextTransferDate);
    }

    @Override
    public void updateStatus(boolean success) {
        if (success) {
            if (isExpired()) {
                this.status = TransferStatus.COMPLETED;
            } else {
                updateNextTransferDate();
            }
        } else {
            increaseFailureCount();
            if (this.failureCount >= 3) {
                this.status = TransferStatus.PAUSED;
            }
        }
    }
}
