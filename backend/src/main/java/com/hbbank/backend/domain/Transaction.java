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
    private Account account;

    @Column(nullable = false)
    private LocalDateTime transactionDateTime;

    @Column(nullable = false, length = 10)
    private String transactionType;

    @Column(nullable = false, length = 100)
    private String sender;

    @Column(nullable = false, length = 100)
    private String receiver;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    private BigDecimal withdrawalAmount;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    private BigDecimal depositAmount;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    private BigDecimal balance;

}
