package com.hbbank.backend.unit.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hbbank.backend.exception.account.InvalidAccountStatusException;

import com.hbbank.backend.exception.transaction.InvalidDateRangeException;
import com.hbbank.backend.exception.user.UserNotFoundException;
import com.hbbank.backend.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import com.hbbank.backend.dto.TransactionSearchDTO;

import com.hbbank.backend.exception.account.AccountNotFoundException;


/*
TransactionService 단위 테스트
거래내역을 생성하는 메서드는 이미 생성에 대한 검증은 끝난 채로 메서드 호출이 될 예정
따라서 거래내역 생성에 대해서는 생성이 되었는지만 확인

Optional.empty() => null 값 반환
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
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
        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        //when
        fa.withdraw(amount);
        ta.deposit(amount);
        List<Transaction> list = transactionService.createTransaction(fa, ta, amount);

        //then
        Transaction withdraw = list.get(0);
        Transaction deposit = list.get(1);

        assertAll(
                () -> assertNotNull(withdraw),
                () -> assertNotNull(deposit),
                () -> assertEquals("출금", withdraw.getTransactionType()),
                () -> assertEquals("입금", deposit.getTransactionType()),
                () -> assertEquals(new BigDecimal("1000"), withdraw.getWithdrawalAmount()),
                () -> assertEquals(new BigDecimal("1000"), deposit.getDepositAmount()),
                () -> assertEquals(new BigDecimal("4000"), withdraw.getBalance()),
                () -> assertEquals(new BigDecimal("2000"), deposit.getBalance())
        );
        verify(transactionRepository, times(2)).saveAndFlush(any(Transaction.class));
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 저장 실패 예외 발생 확인")
    void createTransaction_Fail_DbConnectionFail() {
        //given
        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenThrow(RuntimeException.class);

        //when & then
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 트랜잭션 충돌 예외 발생 확인")
    void createTransaction_Fail_TransactionalCrash() {
        //given
        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenThrow(TransactionSystemException.class);

        //when & then
        assertThrows(TransactionSystemException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 출금 거래내역 생성 실패")
    void createTransaction_Fail_WithdrawTransaction() {
        //given
        when(transactionRepository.saveAndFlush(argThat(t -> t.getTransactionType().equals("출금"))))
                .thenThrow(RuntimeException.class);

        //when & then
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    @Test
    @DisplayName("거래내역 생성 실패 - 입금 거래내역 생성 실패")
    void createTransaction_Fail_DepositTransaction() {
        //given
        when(transactionRepository.saveAndFlush(argThat(t -> t.getTransactionType().equals("입금"))))
                .thenThrow(RuntimeException.class);

        //when & then
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(fa, ta, amount));
        verify(transactionRepository, times(1)).saveAndFlush(any(Transaction.class));
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 성공")
    void findAllByAccountId_Fail_NotDescending() {
        //given
        doNothing()
                .when(accountService)
                .verifyAccount(anyLong());
        when(transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(anyLong()))
                .thenReturn(Optional.of(tlist));

        //when
        Long accountId = 1L;
        List<Transaction> result = transactionService.findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);

        //then
        for (int i = 0; i < result.size() - 1; i++) {
            assertNotNull(result.get(i));
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        assertNotNull(result.get(result.size() - 1));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository).findAllByAccount_IdOrderByTransactionDateTimeDesc(anyLong());
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 성공 - 거래내역 없음")
    void findAllByAccountId_Success_EmptyList() {
        //given
        when(transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(1L))
                .thenReturn(Optional.empty());

        //when
        List<Transaction> res = transactionService.findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);

        //then
        assertTrue(res.isEmpty());
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository).findAllByAccount_IdOrderByTransactionDateTimeDesc(1L);
    }

    @Test
    @DisplayName("계좌 ID 거래내역 조회 실패 - 정상 상태가 아닌 계좌")
    void findAllByAccountId_Fail_NotActiveAccount() {
        //given
        doThrow(InvalidAccountStatusException.class)
                .when(accountService)
                .verifyAccount(anyLong());

        //when & then
        assertThrows(InvalidAccountStatusException.class, () -> transactionService.findAllByAccount_IdOrderByTransactionDateTimeDesc(1L));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository, times(0)).findAllByAccount_IdOrderByTransactionDateTimeDesc(anyLong());
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 모든 거래내역")
    void findAllByCondition_Success_All() {
        // given
        doNothing()
                .when(accountService)
                .verifyAccount(anyLong());
        when(transactionRepository.findAllByCondition(any(TransactionSearchDTO.class)))
                .thenReturn(Optional.of(tlist));

        // when
        List<Transaction> result = transactionService
                .findAllByCondition(allDto);

        // then
        for (int i = 0; i < result.size() - 1; i++) {
            assertNotNull(result.get(i));
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        assertNotNull(result.get(result.size() - 1));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository).findAllByCondition(any(TransactionSearchDTO.class));
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 입금 거래내역")
    void findAllByCondition_Success_Deposit() {
        // given
        doNothing()
                .when(accountService)
                .verifyAccount(anyLong());
        when(transactionRepository.findAllByCondition(any(TransactionSearchDTO.class)))
                .thenReturn(Optional.of(tlist));

        // when
        List<Transaction> result = transactionService
                .findAllByCondition(depositDto);

        // then
        for (int i = 0; i < result.size() - 1; i++) {
            assertNotNull(result.get(i));
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        assertNotNull(result.get(result.size() - 1));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository).findAllByCondition(any(TransactionSearchDTO.class));
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 출금 거래내역")
    void findAllByCondition_Success_Withdraw() {
        // given
        doNothing()
                .when(accountService)
                .verifyAccount(anyLong());
        when(transactionRepository.findAllByCondition(any(TransactionSearchDTO.class)))
                .thenReturn(Optional.of(tlist));

        // when
        List<Transaction> result = transactionService
                .findAllByCondition(withdrawDto);

        // then
        for (int i = 0; i < result.size() - 1; i++) {
            assertNotNull(result.get(i));
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertTrue(a.isAfter(b));
        }
        assertNotNull(result.get(result.size() - 1));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository).findAllByCondition(any(TransactionSearchDTO.class));
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 성공 - 빈 리스트")
    void findAllByCondition_Success_EmptyList() {
        //given
        doNothing()
                .when(accountService)
                .verifyAccount(anyLong());
        when(transactionRepository.findAllByCondition(any(TransactionSearchDTO.class)))
                .thenReturn(Optional.of(List.of()));

        //when
        List<Transaction> result = transactionService
                .findAllByCondition(allDto);

        //then
        assertTrue(result.isEmpty());
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository).findAllByCondition(allDto);
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 존재하지 않는 계좌")
    void findAllByCondition_Fail_NotExisting() {
        //given
        doThrow(new AccountNotFoundException())
                .when(accountService)
                .verifyAccount(anyLong());

        //when&given
        assertThrows(AccountNotFoundException.class, () -> transactionService.findAllByCondition(allDto));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository, times(0)).findAllByCondition(allDto);
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 정상 상태가 아닌 계좌")
    void findAllByCondition_Fail_NotActiveAccount() {
        //given
        doThrow(new InvalidAccountStatusException())
                .when(accountService)
                .verifyAccount(anyLong());

        //when & then
        assertThrows(InvalidAccountStatusException.class, () -> transactionService.findAllByCondition(allDto));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository, times(0)).findAllByCondition(any(TransactionSearchDTO.class));
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 내림차순이 아님")
    void findAllByCondition_Fail_NotDescending() {
        //given
        doNothing()
                .when(accountService)
                .verifyAccount(anyLong());

        // List.of로 생성한 tlist는 불변 객체이므로 ArrayList 로 새 객체 생성해서 쓰기
        ArrayList<Transaction> temp = new ArrayList<>(tlist);
        temp.sort((a, b) -> a.getTransactionDateTime().compareTo(b.getTransactionDateTime()));

        when(transactionRepository.findAllByCondition(any(TransactionSearchDTO.class)))
                .thenReturn(Optional.of(temp));

        //when
        List<Transaction> result = transactionService
                .findAllByCondition(allDto);

        //then
        for (int i = 0; i < result.size() - 1; i++) {
            assertNotNull(result.get(i));
            LocalDateTime a = result.get(i).getTransactionDateTime();
            LocalDateTime b = result.get(i + 1).getTransactionDateTime();
            assertFalse(a.isAfter(b));
        }
        assertNotNull(result.get(result.size() - 1));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository).findAllByCondition(any(TransactionSearchDTO.class));
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 종료일이 앞섬")
    void findAllByCondition_Fail_EndDateAhead() {
        //given
        doNothing()
                .when(accountService)
                .verifyAccount(anyLong());
        TransactionSearchDTO wrong = TransactionSearchDTO.builder()
                .accountId(1L)
                .startDate("2024-12-20")
                .endDate("1999-06-29")
                .build();

        //when & then
        assertThrows(InvalidDateRangeException.class,
                () -> transactionService.findAllByCondition(wrong));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository, times(0)).findAllByCondition(any(TransactionSearchDTO.class));
    }

    @Test
    @DisplayName("특정 조건으로 거래내역 조회 실패 - 존재하지 않는 사용자")
    void findAllByCondition_Fail_UserNotFound() {
        // given
        doThrow(UserNotFoundException.class)
                .when(accountService)
                .verifyAccount(anyLong());

        //when&then
        assertThrows(UserNotFoundException.class, () -> transactionService.findAllByCondition(allDto));
        verify(accountService).verifyAccount(anyLong());
        verify(transactionRepository, times(0)).findAllByCondition(allDto);
    }

}
