package com.hbbank.backend.domain;

import java.math.BigDecimal;

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
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

    @Column(nullable = false)
    @Size(min = 1, max = 50, message = "계좌명은 1자 이상 50자 이하여야 합니다")
    @NotBlank(message = "계좌명은 필수입니다")
    private String accountName;

    @Column(nullable = false, unique = true, length = 15)
    @Pattern(regexp = "^[0-9]{15}$", message = "계좌번호는 15자리 숫자여야 합니다")
    private String accountNumber;

    @Column(nullable = false, columnDefinition = "DECIMAL(19,4)")
    @ColumnDefault("0")
    @PositiveOrZero(message = "잔액은 0 이상이어야 합니다")
    private BigDecimal balance;

    @Column(nullable = false, columnDefinition = "DOUBLE(4,2)")
    @ColumnDefault("0.0")
    @PositiveOrZero(message = "이자율은 0 이상이어야 합니다")
    @Max(value = 100, message = "이자율은 100을 초과할 수 없습니다")
    private Double interestRate;

    @Column(nullable = false)
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

}
