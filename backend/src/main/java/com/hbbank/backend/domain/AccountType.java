package com.hbbank.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "account_type")
public class AccountType {

    @Id
    private String code;
    
    private String name;
    private String description;
    private Double interestRate;
    private Long minimumBalance;

}
