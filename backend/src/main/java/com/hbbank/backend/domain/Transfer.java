package com.hbbank.backend.domain;

import java.math.BigDecimal;

import com.hbbank.backend.domain.enums.TransferStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    protected Account fromAccount;

    @Column(nullable = false, length = 15)
    protected String toAccountNumber;

    @Column(nullable = false)
    protected BigDecimal amount;

    @Column(length = 100)
    protected String description;

    @Column(nullable = false)
    protected int failureCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    protected TransferStatus status;

    public void increaseFailureCount() {
        this.failureCount++;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    // 추상 메서드
    public abstract boolean isExecutable();

    public abstract void updateStatus(boolean success);
}
