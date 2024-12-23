package com.hbbank.backend.domain;

import java.time.LocalDate;

import com.hbbank.backend.domain.enums.TransferStatus;
import com.hbbank.backend.dto.AutoTransferRequestDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    @Column(nullable = false)
    private int transferDay;                          // 매월 자동이체 실행일 (1-31)

    @Column(nullable = false)
    private LocalDate nextTransferDate;              // 다음 자동이체 실행 예정일

    @Column(nullable = false)
    private LocalDate startDate;                     // 자동이체 시작일

    @Column(nullable = false)
    private LocalDate endDate;                       // 자동이체 종료일

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
