package com.hbbank.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.domain.enums.AccountStatus;
import com.hbbank.backend.domain.enums.TransferType;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.exception.account.*;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

/*
 * TransferService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
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
    private TransferRequestDTO dto1;
    private TransferRequestDTO dto2;

    @BeforeEach
    @DisplayName("출금 계좌, 입금 계좌, 거래액, 이체 요청 생성")
    void init() {
        fa = Account.builder()
                .id(1L)
                .status(AccountStatus.ACTIVE)
                .accountNumber("9876543210")
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

        dto1 = TransferRequestDTO.builder()
                .type(TransferType.INSTANT)
                .fromAccountId(fa.getId())
                .toAccountNumber(ta.getAccountNumber())
                .amount(amount)
                .password("1234")
                .build();
        dto2 = TransferRequestDTO.builder()
                .type(TransferType.INSTANT)
                .fromAccountId(ta.getId())
                .toAccountNumber(fa.getAccountNumber())
                .amount(amount)
                .password("1234")
                .build();
    }

    @Test
    @DisplayName("이체 성공")
    void executeTransfer_Success() {
        //given
        when(accountRepository.findById(fa.getId())).thenReturn(Optional.of(fa));
        when(accountRepository.findByIdWithLock(fa.getId())).thenReturn(Optional.of(fa));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(true);
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(new Account());
        when(transactionService.createTransaction(fa, ta, amount)).thenReturn(new ArrayList<>());

        //when
        // 이체 실행
        boolean result = transferService.transfer(dto1);

        //then
        assertAll(
                () -> assertTrue(result),
                () -> assertEquals(new BigDecimal("4000"), fa.getBalance()),
                () -> assertEquals(new BigDecimal("2000"), ta.getBalance())
        );
        verify(accountRepository).findById(any());
        verify(accountRepository).findByIdWithLock(any());
        verify(accountRepository).findByAccountNumberWithLock(any());
        verify(accountRepository, times(2)).saveAndFlush(any());
    }

    @Test
    @DisplayName("계좌 락 획득 성공 - 출금 계좌 -> 입금 계좌")
    void getLock_Success_WithdrawLessThanDeposit() {
        //given
        when(accountRepository.findById(ta.getId())).thenReturn(Optional.of(ta));
        when(accountRepository.findByAccountNumberWithLock(fa.getAccountNumber())).thenReturn(Optional.of(fa));
        when(accountRepository.findByIdWithLock(ta.getId())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(true);
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(new Account());

        //when
        boolean result = transferService.transfer(dto2);

        //then
        InOrder order = inOrder(accountRepository);
        order.verify(accountRepository).findByIdWithLock(ta.getId());
        order.verify(accountRepository).findByAccountNumberWithLock(fa.getAccountNumber());
        assertTrue(result);
    }

    @Test
    @DisplayName("계좌 락 획득 성공 - 입금 계좌 -> 출금 계좌")
    void getLock_Success_DepositLessThanWithdraw() {
        //given
        when(accountRepository.findById(fa.getId())).thenReturn(Optional.of(fa));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(fa));
        when(accountRepository.findByIdWithLock(fa.getId())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(true);
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(null);

        //when
        boolean result = transferService.transfer(dto1);

        //then
        InOrder order = inOrder(accountRepository);
        order.verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        order.verify(accountRepository).findByIdWithLock(fa.getId());
        assertTrue(result);
    }

    // 계좌 락 획득 실패는 출금 계좌번호가 입금 계좌번호보다 작을 때를 가정함
    @Test
    @DisplayName("계좌 락 획득 실패 - 출금 계좌 없음")
    void getLock_Fail_WithdrawAccountNotFound() {
        //given
        Long accountId = dto1.getFromAccountId();
        when(accountRepository.findById(accountId)).thenThrow(new AccountNotFoundException());

        //when&then
        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(accountId);
        verify(accountRepository, times(0)).findByIdWithLock(accountId);
        verify(accountRepository, times(0)).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("계좌 락 획득 실패 - 입금 계좌 없음")
    void getLock_Fail_DepositAccountNotFound() {
        //given
        Long accountId = dto1.getFromAccountId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(fa));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenThrow(new AccountNotFoundException());

        //when&then
        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(accountId);
        verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("비밀번호 확인 실패 - 비밀번호 불일치")
    void checkPassword_Fail_InvalidPassword() {
        //given
        when(accountRepository.findById(fa.getId())).thenReturn(Optional.of(fa));
        when(accountRepository.findByIdWithLock(fa.getId())).thenReturn(Optional.of(fa));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(false);

        //when &then
        assertThrows(InvalidAccountPasswordException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(fa.getId());
        verify(accountRepository).findByIdWithLock(fa.getId());
        verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("이체 실행 실패 - 유효하지 않은 출금 계좌 상태")
    void executeTransfer_Fail_InvalidWithdrawAccountStatus() {
        //given
        Account inactive = Account.builder()
                .id(fa.getId())
                .accountNumber(fa.getAccountNumber())
                .balance(fa.getBalance())
                .status(AccountStatus.DORMANT)  // 휴면
                .build();

        when(accountRepository.findById(inactive.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByIdWithLock(inactive.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(true);

        //when & then
        assertThrows(InvalidAccountStatusException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(any());
        verify(accountRepository).findByIdWithLock(any());
        verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("이체 실행 실패 - 유효하지 않은 입금 계좌 상태")
    void executeTransfer_Fail_InvalidDepositAccountStatus() {
        //given
        Account inactive = Account.builder()
                .id(ta.getId())
                .accountNumber(ta.getAccountNumber())
                .balance(ta.getBalance())
                .status(AccountStatus.DORMANT)  // 휴면
                .build();

        when(accountRepository.findById(fa.getId())).thenReturn(Optional.of(fa));
        when(accountRepository.findByIdWithLock(fa.getId())).thenReturn(Optional.of(fa));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(inactive));
        when(encoder.matches(any(), any())).thenReturn(true);

        //when & then
        assertThrows(InvalidAccountStatusException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(any());
        verify(accountRepository).findByIdWithLock(any());
        verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("이체 실행 실패 - 1회 이체한도 초과")
    void executeTransfer_Fail_TransferLimitExceeded() {
        //given
        Account inactive = Account.builder()
                .id(fa.getId())
                .accountNumber(fa.getAccountNumber())
                .balance(fa.getBalance())
                .status(AccountStatus.ACTIVE)
                .transferLimit(new BigDecimal("0"))
                .build();

        when(accountRepository.findById(fa.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByIdWithLock(fa.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(true);

        //when & then
        assertThrows(TransferLimitExceededException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(any());
        verify(accountRepository).findByIdWithLock(any());
        verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("이체 실행 실패 - 일일 이체한도 초과")
    void executeTransfer_Fail_ValidateDailyTransferLimit() {
        //given
        Account inactive = Account.builder()
                .id(fa.getId())
                .accountNumber(fa.getAccountNumber())
                .balance(fa.getBalance())
                .status(AccountStatus.ACTIVE)
                .transferLimit(new BigDecimal("10000"))
                .dailyTransferLimit(new BigDecimal("0"))
                .dailyTransferredAmount(new BigDecimal("0"))
                .build();

        when(accountRepository.findById(fa.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByIdWithLock(fa.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(true);

        //when & then
        assertThrows(DailyTransferLimitExceededException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(any());
        verify(accountRepository).findByIdWithLock(any());
        verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

    @Test
    @DisplayName("이체 실행 실패 - 잔액 부족")
    void executeTransfer_Fail_ValidateBalance() {
        //given
        Account inactive = Account.builder()
                .id(fa.getId())
                .accountNumber(fa.getAccountNumber())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .transferLimit(new BigDecimal("10000"))
                .dailyTransferLimit(new BigDecimal("9999999999"))
                .dailyTransferredAmount(new BigDecimal("0"))
                .build();

        when(accountRepository.findById(fa.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByIdWithLock(fa.getId())).thenReturn(Optional.of(inactive));
        when(accountRepository.findByAccountNumberWithLock(ta.getAccountNumber())).thenReturn(Optional.of(ta));
        when(encoder.matches(any(), any())).thenReturn(true);

        //when & then
        assertThrows(OutofBalanceException.class, () -> transferService.transfer(dto1));
        verify(accountRepository).findById(any());
        verify(accountRepository).findByIdWithLock(any());
        verify(accountRepository).findByAccountNumberWithLock(ta.getAccountNumber());
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRepository, times(0)).saveAndFlush(any(Account.class));
    }

}
