package com.hbbank.backend.service;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.domain.enums.AccountStatus;
import com.hbbank.backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/*
 * TransferService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private AccountService accountService;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private TransferService transferService;

    private Account fa, ta;
    private BigDecimal amount;

    @BeforeEach
    @DisplayName("출금 계좌, 입금 계좌, 거래액 생성")
    void init() {
        fa = Account.builder()
                .id(1L)
                .status(AccountStatus.ACTIVE)
                .accountNumber("0987654321")
                .password("encodedPassword")
                .balance(new BigDecimal("5000"))
                .user(User.builder().id(1L).build())
                .password(encoder.encode("1234"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        ta = Account.builder()
                .id(2L)
                .status(AccountStatus.ACTIVE)
                .accountNumber("1234567890")
                .balance(new BigDecimal("1000"))
                .user(User.builder().id(2L).build())
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        amount = new BigDecimal("1000");
    }

    @Test
    @DisplayName("계좌 락 획득 성공 - 출금 계좌번호 > 입금 계좌번호")
    void getLock_Success_WithdrawGreaterThanDeposit() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("계좌 락 획득 성공 - 출금 계좌번호 < 입금 계좌번호")
    void getLock_Success_WithdrawLessThanDeposit() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("계좌 락 획득 실패 - 출금 계좌 없음")
    void getLock_Fail_WithdrawAccountNotFound() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("계좌 락 획득 실패 - 입금 계좌 없음")
    void getLock_Fail_DepositAccountNotFound() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("비밀번호 확인 성공")
    void checkPassword_Success() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("비밀번호 확인 실패 - 비밀번호 불일치")
    void checkPassword_Fail_InvalidPassword() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("이체 실행 성공")
    void executeTransfer_Success() {
        // TODO document why this method is empty
        /*
        출금 계좌 출금 -> 입금 계좌 입금 -> 거래내여 생성 -> 계좌 저장까지
         */
    }

    @Test
    @DisplayName("이체 실행 실패 - 유효하지 않은 출금 계좌 상태")
    void executeTransfer_Fail_InvalidWithdrawAccountStatus() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("이체 실행 실패 - 유효하지 않은 입금 계좌 상태")
    void executeTransfer_Fail_InvalidDepositAccountStatus() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("이체 실행 실패 - 1회 이체한도 초과")
    void executeTransfer_Fail_TransferLimitExceeded() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("이체 실행 실패 - 1일 이체한도 초과")
    void executeTransfer_Fail_ValidateDailyTransferLimit() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("이체 실행 실패 - 잔액 부족")
    void executeTransfer_Fail_ValidateBalance() {
        // TODO document why this method is empty
    }

}
