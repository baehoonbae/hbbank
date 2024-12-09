package com.hbbank.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "계좌 정보는 필수입니다")
    private Account account;

    @Column(nullable = false)
    @NotNull(message = "거래일시는 필수입니다")
    @PastOrPresent(message = "거래일시는 현재 또는 과거 시점이어야 합니다")
    private LocalDateTime transactionDateTime;

    @Column(nullable = false, length = 10)
    @NotBlank(message = "거래유형은 필수입니다")
    @Pattern(regexp = "^(입금|출금)$", message = "거래유형은 '입금' 또는 '출금'이어야 합니다")
    private String transactionType;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "보낸분 정보는 필수입니다")
    @Size(max = 100, message = "보낸분 정보는 100자를 초과할 수 없습니다")
    private String sender;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "받는분 정보는 필수입니다")
    @Size(max = 100, message = "받는분 정보는 100자를 초과할 수 없습니다")
    private String receiver;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    @PositiveOrZero(message = "출금액은 0 이상이어야 합니다")
    private BigDecimal withdrawalAmount;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    @PositiveOrZero(message = "입금액은 0 이상이어야 합니다")
    private BigDecimal depositAmount;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    @PositiveOrZero(message = "잔액은 0 이상이어야 합니다")
    private BigDecimal balance;

}
