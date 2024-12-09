package com.hbbank.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "account_type")
public class AccountType {

    @Id
    @Column(length = 10)
    private String code;
    
    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 4, scale = 2)
    private Double interestRate;

    @Column(nullable = false)
    private Long minimumBalance;

}
