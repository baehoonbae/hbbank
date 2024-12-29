package com.hbbank.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hbbank.backend.dto.TransactionSearchDTO;
import org.junit.jupiter.api.Assertions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hbbank.backend.exception.InvalidAccountStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.domain.enums.AccountStatus;
import com.hbbank.backend.repository.TransactionRepository;
import org.springframework.transaction.TransactionSystemException;


/*
TransactionService 단위 테스트
거래내역을 생성하는 메서드는 이미 생성에 대한 검증은 끝난 채로 메서드 호출이 될 예정
따라서 거래내역 생성에 대해서는 생성이 되었는지만 확인
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private TransactionService transactionService;

    private Account fa;
    private Account ta;
    private BigDecimal amount;
    private List<Transaction> tlist;
    private TransactionSearchDTO allDto;
    private TransactionSearchDTO withdrawDto;
    private TransactionSearchDTO depositDto;

    @BeforeEach
    @DisplayName("출금 계좌, 입금 계좌, 거래내역, 거래내역 조건 생성")
    void createAccounts() {
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

        allDto = TransactionSearchDTO.builder()
                .userId(1L)
                .accountId(1L)
                .startDate("2020-12-20")
                .endDate("2024-12-29")
                .transactionType(0)
                .page(0)
                .build();

        depositDto = TransactionSearchDTO.builder()
                .userId(1L)
                .accountId(1L)
                .startDate("2020-12-20")
                .endDate("2024-12-29")
                .transactionType(1)
                .page(0)
                .build();

        withdrawDto = TransactionSearchDTO.builder()
                .userId(1L)
                .accountId(1L)
                .startDate("2020-12-20")
                .endDate("2024-12-29")
                .transactionType(2)
                .page(0)
                .build();

        LocalDateTime now = LocalDateTime.now();
        tlist = List.of(
                Transaction.builder()
                        .transactionDateTime(now)
                        .transactionType("출금")
                        .build(),
                Transaction.builder()
                        .transactionDateTime(now.minusMinutes(1))
                        .transactionType("입금")
                        .build(),
                Transaction.builder()
                        .transactionDateTime(now.minusMinutes(2))
                        .transactionType("출금")
                        .build(),
                Transaction.builder()
                        .transactionDateTime(now.minusMinutes(3))
                        .transactionType("입금")
                        .build()
        );

        amount = new BigDecimal("1000");
    }

    @Test
    @DisplayName("거래내역 생성 성공")
    void createTransaction_Success() {
        //given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        //when
        fa.withdraw(amount);
        ta.deposit(amount);
        transactionService.createTransaction(fa, ta, amount);

        //then
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        List<Transaction> list = captor.getAllValues();
        Transaction withdraw = list.get(0);
        Transaction deposit = list.get(1);

        assertEquals("출금", withdraw.getTransactionType());
        assertEquals("입금", deposit.getTransactionType());

        assertEquals(new BigDecimal("1000"), withdraw.getWithdrawalAmount());
        assertEquals(new BigDecimal("1000"), deposit.getDepositAmount());

        assertEquals(new BigDecimal("4000"), withdraw.getBalance());
        assertEquals(new BigDecimal("2000"), deposit.getBalance());
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 저장 실패 예외 발생 확인")
    void createTransaction_Fail_DbConnectionFail() {
        //given
        when(transactionRepository.save(any(Transaction.class))).thenThrow(RuntimeException.class);

        //when & then
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 트랜잭션 충돌 예외 발생 확인")
    void createTransaction_Fail_TransactionalCrash() {
        //given
        when(transactionRepository.save(any(Transaction.class))).thenThrow(TransactionSystemException.class);

        //when & then
        assertThrows(TransactionSystemException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 출금 거래내역 생성 실패")
    void createTransaction_Fail_WithdrawTransaction() {
        //given
        doThrow(new RuntimeException("출금 거래내역 저장 실패"))
                .when(transactionRepository)
                .save(argThat(t -> t.getTransactionType().equals("출금")));

        //when & then
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 입금 거래내역 생성 실패")
    void createTransaction_Fail_DepositTransaction() {
        //given
        doThrow(new RuntimeException("입금 거래내역 저장 실패"))
                .when(transactionRepository)
                .save(argThat(t -> t.getTransactionType().equals("입금")));

        //when & then
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 성공")
    void findAllByAccountId_Fail_NotDescending() throws Exception {
        //given
        Long accountId = 1L;
        doNothing().when(accountService).verifyAccount(accountId);
        when(transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId))
                .thenReturn(Optional.of(tlist));

        //when
        Optional<List<Transaction>> res = transactionService
                .findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);
        assertTrue(res.isPresent());
        List<Transaction> result = res.get();

        //then
        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        verify(transactionRepository).findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 성공 - NULL 이 아닌 리스트")
    void findAllByAccountId_Success_NotNull() throws Exception {
        //given
        when(transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(anyLong()))
                .thenReturn(Optional.of(List.of(new Transaction())));

        //when
        Optional<List<Transaction>> res = transactionService
                .findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);

        //then
        assertTrue(res.isPresent());
        verify(transactionRepository).findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 성공 - 거래내역 없음")
    void findAllByAccountId_Success_EmptyList() throws Exception {
        //given
        when(transactionService.findAllByAccount_IdOrderByTransactionDateTimeDesc(1L))
                .thenReturn(Optional.of(List.of()));

        //when
        Optional<List<Transaction>> res = transactionService
                .findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);

        //then
        assertTrue(res.isPresent());
        assertTrue(res.get().isEmpty());
        verify(transactionRepository).findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 실패 - NULL 값")
    void findAllByAccountId_Fail_IsNull() throws Exception {
        //given
        when(transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(1L)).thenReturn(Optional.empty());

        //when
        Optional<List<Transaction>> res = transactionService
                .findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);

        //then
        assertTrue(res.isEmpty());
        verify(transactionRepository).findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 실패 - 정상 상태가 아닌 계좌")
    void findAllByAccountId_Fail_NotActiveAccount() {
        //given
        when(transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(1L)).thenThrow(InvalidAccountStatusException.class);

        //when & then
        assertThrows(InvalidAccountStatusException.class, () -> transactionService.findAllByAccount_IdOrderByTransactionDateTimeDesc(1L));
        verify(transactionRepository).findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 모든 거래내역")
    void findAllByCondition_Success_All() throws Exception {
        // given
        Long accountId = allDto.getAccountId();
        doNothing().when(accountService).verifyAccount(accountId);
        when(transactionRepository.findAllByCondition(allDto)).thenReturn(Optional.of(tlist));

        // when
        Optional<List<Transaction>> res = transactionService
                .findAllByCondition(allDto);
        assertTrue(res.isPresent());
        List<Transaction> result = res.get();

        // then
        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        verify(transactionRepository).findAllByCondition(allDto);
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 입금 거래내역")
    void findAllByCondition_Success_Deposit()throws Exception {
        // given
        Long accountId = depositDto.getAccountId();
        doNothing().when(accountService).verifyAccount(accountId);
        when(transactionRepository.findAllByCondition(depositDto)).thenReturn(Optional.of(tlist));

        // when
        Optional<List<Transaction>> res = transactionService
                .findAllByCondition(depositDto);
        assertTrue(res.isPresent());
        List<Transaction> result = res.get();

        // then
        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        verify(transactionRepository).findAllByCondition(depositDto);
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 출금 거래내역")
    void findAllByCondition_Success_Withdraw() throws Exception {
        // given
        Long accountId = withdrawDto.getAccountId();
        doNothing().when(accountService).verifyAccount(accountId);
        when(transactionRepository.findAllByCondition(withdrawDto)).thenReturn(Optional.of(tlist));

        // when
        Optional<List<Transaction>> res = transactionService
                .findAllByCondition(withdrawDto);
        assertTrue(res.isPresent());
        List<Transaction> result = res.get();

        // then
        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        verify(transactionRepository).findAllByCondition(withdrawDto);
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - NULL 이 아닌 리스트")
    void findAllByCondition_Success_NotNull() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 거래내역 없음")
    void findAllByCondition_Success_EmptyList() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 존재하지 않는 계좌")
    void findAllByCondition_Fail_NotExisting() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - NULL 값")
    void findAllByCondition_Fail_IsNull() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 정상 상태가 아닌 계좌")
    void findAllByCondition_Fail_NotActiveAccount() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 내림차순이 아님")
    void findAllByCondition_Fail_NotDescending() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 종료일이 앞섬")
    void findAllByCondition_Fail_EndDateAhead() {
        // TODO document why this method is empty
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 존재하지 않는 사용자")
    void findAllByCondition_Fail_UserNotFound() {
        // TODO document why this method is empty
    }

}
