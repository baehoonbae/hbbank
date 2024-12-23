package com.hbbank.backend.domain;

import java.math.BigDecimal;

import org.hibernate.annotations.ColumnDefault;

import com.hbbank.backend.domain.enums.AccountStatus;
import com.hbbank.backend.exception.DailyTransferLimitExceededException;
import com.hbbank.backend.exception.InvalidAccountStatusException;
import com.hbbank.backend.exception.OutofBalanceException;
import com.hbbank.backend.exception.TransferLimitExceededException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_type_code", nullable = false, columnDefinition = "varchar(10)")
    private AccountType accountType;

    @Column(nullable = false, length = 50)
    private String accountName;

    @Column(nullable = false, unique = true, length = 15)
    private String accountNumber;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    private BigDecimal balance;

    @Column(nullable = false, columnDefinition = "DOUBLE(4,2)")
    @ColumnDefault("0.0")
    private Double interestRate;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false)
    private BigDecimal dailyTransferLimit;

    @Column(nullable = false)
    private BigDecimal transferLimit;

    @Column(nullable = false)
    private BigDecimal dailyTransferredAmount;

    @PrePersist
    private void setDefaultValues() {
        this.status = AccountStatus.ACTIVE;
        this.dailyTransferLimit = this.accountType.getDefaultDailyTransferLimit();
        this.transferLimit = this.accountType.getDefaultTransferLimit();
        this.dailyTransferredAmount = BigDecimal.ZERO;
    }

    public void withdraw(BigDecimal amount) {
        validateAccountStatus();
        validateTransferLimit(amount);
        validateDailyTransferLimit(amount);
        validateBalance(amount);
        this.balance = this.balance.subtract(amount);
        this.dailyTransferredAmount = this.dailyTransferredAmount.add(amount);
    }

    private void validateAccountStatus() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException("유효하지 않은 계좌 상태입니다: " + this.status);
        }
    }

    private void validateTransferLimit(BigDecimal amount) {
        if (amount.compareTo(this.transferLimit) > 0) {
            throw new TransferLimitExceededException("1회 이체한도를 초과했습니다");
        }
    }

    private void validateDailyTransferLimit(BigDecimal amount) {
        if (this.dailyTransferredAmount.add(amount).compareTo(this.dailyTransferLimit) > 0) {
            throw new DailyTransferLimitExceededException("일일 이체한도를 초과했습니다");
        }
    }

    private void validateBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new OutofBalanceException("잔액이 부족합니다");
        }
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

}
