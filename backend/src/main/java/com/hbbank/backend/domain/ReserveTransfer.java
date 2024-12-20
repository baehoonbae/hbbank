package com.hbbank.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hbbank.backend.domain.enums.ReserveTransferStatus;
import com.hbbank.backend.dto.ReserveTransferRequestDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reserve_transfer")
public class ReserveTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                   // 예약이체 고유 식별자

    @NotNull(message = "사용자 ID는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                                // 예약이체 등록 사용자

    @NotNull(message = "출금 계좌는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;                      // 출금 계좌 정보

    @NotBlank(message = "입금 계좌번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{15}$", message = "입금 계좌번호는 15자리 숫자여야 합니다")
    private String toAccountNumber;                   // 입금 계좌번호

    @NotNull(message = "이체 금액은 필수입니다")
    @Positive(message = "이체 금액은 0보다 커야 합니다")
    private BigDecimal amount;                        // 이체 금액

    @NotBlank(message = "이체 설명은 필수입니다")
    private String description;                       // 이체 설명

    @NotNull(message = "예약 실행일시는 필수입니다")
    private LocalDateTime reservedAt;                 // 예약된 실행 일시

    @Enumerated(EnumType.STRING)
    private ReserveTransferStatus status;            // 예약이체 상태 (PENDING, COMPLETED, PAUSED)

    private LocalDateTime completedAt;                // 실행 완료 일시

    @NotNull(message = "실패 횟수는 필수입니다")
    @Min(value = 0, message = "실패 횟수는 0 이상이어야 합니다")
    private int failureCount;                         // 이체 실패 횟수 - 재시도 정책에 사용

    public void update(Account fromAccount, ReserveTransferRequestDTO dto) {
        this.fromAccount = fromAccount;
        this.toAccountNumber = dto.getToAccountNumber();
        this.amount = dto.getAmount();
        this.description = dto.getDescription();
        this.reservedAt = dto.getReservedAt();
        this.status = ReserveTransferStatus.PENDING;
        this.user = fromAccount.getUser();
    }

    // 실패 횟수 증가 메서드
    public void increaseFailureCount() {
        this.failureCount++;
        if (this.failureCount >= 3) {  // 예: 3회 실패시 최종 실패 처리
            this.status = ReserveTransferStatus.PAUSED;
        }
    }

    public void updateStatusCompleted() {
        this.status = ReserveTransferStatus.COMPLETED;
    }

}
