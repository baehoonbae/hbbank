package com.hbbank.backend.domain;

import java.time.LocalDateTime;

import com.hbbank.backend.domain.enums.TransferStatus;
import com.hbbank.backend.dto.ReserveTransferRequestDTO;

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
@Table(name = "reserve_transfer")
public class ReserveTransfer extends Transfer {

    @Column(nullable = false)
    private LocalDateTime reservedAt;                 // 예약된 실행 일시

    private LocalDateTime completedAt;                // 실행 완료 일시

    public void update(Account fromAccount, ReserveTransferRequestDTO dto) {
        this.user = fromAccount.getUser();
        this.fromAccount = fromAccount;
        this.toAccountNumber = dto.getToAccountNumber();
        this.amount = dto.getAmount();
        this.description = dto.getDescription();
        this.status = TransferStatus.ACTIVE;

        this.reservedAt = dto.getReservedAt();
    }

    @Override
    public boolean isExecutable() {
        return status == TransferStatus.ACTIVE && LocalDateTime.now().isAfter(reservedAt);
    }

    @Override
    public void updateStatus(boolean success) {
        if (success) {
            this.status = TransferStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        } else {
            this.failureCount++;
            if (this.failureCount >= 3) {
                this.status = TransferStatus.PAUSED;
            }
        }
    }

}
