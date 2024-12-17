package com.hbbank.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hbbank.backend.domain.enums.AutoTransferStatus;
import com.hbbank.backend.dto.AutoTransferRequestDTO;

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
import jakarta.validation.constraints.Max;
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
@Table(name = "auto_transfer")
public class AutoTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                   // 자동이체 고유 식별자

    @NotNull(message = "사용자 ID는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                                // 자동이체 등록 사용자

    @NotNull(message = "출금 계좌는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;                       // 출금 계좌 정보(자동으로 넘어올 예정)

    @NotBlank(message = "입금 계좌번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{15}$", message = "입금 계좌번호는 15자리 숫자여야 합니다")
    private String toAccountNumber;                    // 입금 계좌번호(사용자 입력)

    @NotNull(message = "이체 금액은 필수입니다")
    @Positive(message = "이체 금액은 0보다 커야 합니다")
    private BigDecimal amount;                        // 이체 금액(사용자 입력)

    @NotBlank(message = "이체 설명은 필수입니다")
    private String description;                       // 이체 설명(사용자 입력)

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

    @NotNull(message = "자동이체 상태는 필수입니다")
    @Enumerated(EnumType.STRING)
    private AutoTransferStatus status;                // 자동이체 상태 (ACTIVE, PAUSED, COMPLETED, FAILED)

    @NotNull(message = "실패 횟수는 필수입니다")
    @Min(value = 0, message = "실패 횟수는 0 이상이어야 합니다")
    private int failureCount;                         // 자동이체 실패 횟수 - 재시도 정책에 사용

    public void update(Account fromAccount, AutoTransferRequestDTO dto) {
        this.fromAccount = fromAccount;
        this.toAccountNumber = dto.getToAccountNumber();
        this.amount = dto.getAmount();
        this.description = dto.getDescription();
        this.transferDay = dto.getTransferDay();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.nextTransferDate = dto.getStartDate().withDayOfMonth(dto.getTransferDay());
        this.status = AutoTransferStatus.ACTIVE;
        this.user = fromAccount.getUser();
    }

    public void updateNextTransferDate() {            // 다음 자동이체 실행일을 한 달 뒤로 업데이트
        this.nextTransferDate = this.nextTransferDate.plusMonths(1)
                .withDayOfMonth(this.transferDay);
    }

    public void increaseFailureCount() {
        this.failureCount++;
    }

    public void setStatusPaused() {
        this.status = AutoTransferStatus.PAUSED;
    }

    public void setStatusCompleted() {
        this.status = AutoTransferStatus.COMPLETED;
    }

    public boolean isExpired() {                      // 자동이체 만료 여부 확인
        return endDate != null && LocalDate.now().isAfter(endDate);
    }
}
