package com.hbbank.backend.domain;

import java.math.BigDecimal;

import org.hibernate.annotations.ColumnDefault;

import com.hbbank.backend.exception.OutofBalanceException;

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

    // Setter를 쓰지 않으려 하기 때문에 Entity 내부에 잔액 관련 메서드만(가장 핵심 필드이기 때문) 정의
    // BigDecimal은 새로운 객체를 반환하기 때문에 다시 할당시켜줘야한다.
    public void withdraw(BigDecimal amount) {
        // 현재 잔액이 부족하면 예외 던지기
        if (this.balance.compareTo(amount) == -1) {
            throw new OutofBalanceException("잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

}
