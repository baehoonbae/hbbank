package com.hbbank.backend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
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

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;

    private LocalDateTime transactionDateTime; // 거래일시
    
    private String transactionType; // 거래유형(입금/출금)
    
    private String sender; // 보낸분
    
    private String receiver; // 받는분
    
    private BigDecimal withdrawalAmount; // 출금액(원)
    
    private BigDecimal depositAmount; // 입금액(원)
    
    private BigDecimal balance; // 잔액(원)
}
