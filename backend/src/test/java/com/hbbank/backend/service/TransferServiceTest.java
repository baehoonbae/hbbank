package com.hbbank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.hbbank.backend.config.TestDataConfig;
import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Import(TestDataConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @RequiredArgsConstructor // 스프링 테스트 클래스는 다른 생명주기를 가진다.. Autowired 직접 달기
@ActiveProfiles("test")
@Slf4j
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestDataConfig testDataConfig;

    private void verifyTransferResult(
            Account fromAccount, // 출금 계좌
            Account toAccount, // 입금 계좌
            BigDecimal initialFromBalance, // 출금 계좌 초기 잔액
            BigDecimal initialToBalance, // 입금 계좌 초기 잔액
            BigDecimal transferAmount // 이체 금액
    ) {
        // 1. 출금 계좌 잔액 검증
        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).get();
        assertEquals(
                initialFromBalance.subtract(transferAmount).stripTrailingZeros(),
                updatedFromAccount.getBalance().stripTrailingZeros(),
                "출금 계좌 잔액이 정확히 차감되어야 함");

        // 2. 입금 계좌 잔액 검증
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).get();
        assertEquals(
                initialToBalance.add(transferAmount).stripTrailingZeros(),
                updatedToAccount.getBalance().stripTrailingZeros(),
                "입금 계좌 잔액이 정확히 증가되어야 함");

        // 3. 거래 내역 검증
        // 출금 거래 내역 확인
        List<Transaction> withdrawals = transactionRepository
                .findByAccountAndTransactionType(fromAccount, "출금").get();
        assertTrue(withdrawals.stream().anyMatch(t -> t.getWithdrawalAmount().equals(transferAmount) &&
                t.getBalance().equals(updatedFromAccount.getBalance())), "출금 거래 내역이 올바르게 생성되어야 함");

        // 입금 거래 내역 확인
        List<Transaction> deposits = transactionRepository
                .findByAccountAndTransactionType(toAccount, "입금").get();
        assertTrue(deposits.stream().anyMatch(t -> t.getDepositAmount().equals(transferAmount) &&
                t.getBalance().equals(updatedToAccount.getBalance())), "입금 거래 내역이 올바르게 생성되어야 함");
    }

    @BeforeAll
    void setup() {
        // 스프링 컨텍스트가 완전히 초기화된 후에 실행되도록 보장
        try {
            testDataConfig.cleanupBean().run(null);
            testDataConfig.init().run(null);
        } catch (Exception e) {
            throw new RuntimeException("테스트 데이터 초기화 실패", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 기본 순차적인 이체 테스트")
    @Transactional
    void basicTransferTest() {
        // given
        String fromAccountNumber = String.format("%015d", 1);
        String toAccountNumber = String.format("%015d", 2);
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber).get();
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber).get();
        BigDecimal transferAmount = new BigDecimal("1000");

        BigDecimal fromInitialBalance = fromAccount.getBalance();
        BigDecimal toInitialBalance = toAccount.getBalance();

        // when
        TransferRequestDTO dto = TransferRequestDTO.builder()
                .fromAccountId(fromAccount.getId())
                .toAccountNumber(toAccount.getAccountNumber())
                .amount(transferAmount)
                .password("1234")
                .build();

        transferService.executeTransfer(dto);

        // then
        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).get();
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).get();

        assertEquals(fromInitialBalance.subtract(transferAmount), updatedFromAccount.getBalance());
        assertEquals(toInitialBalance.add(transferAmount), updatedToAccount.getBalance());
    }

    @Test
    @Order(2)
    @DisplayName("2. 동시 다중 이체 테스트 (B,C,D -> A)")
    void concurrentMultipleTransferTest() throws InterruptedException {
        // given
        int threadCount = 3; // B, C, D 세 계좌에서 동시 이체
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Account targetAccount = accountRepository.findByAccountNumber(String.format("%015d", 1)).get();
        BigDecimal initialBalance = targetAccount.getBalance();
        BigDecimal transferAmount = new BigDecimal("1000");

        List<Account> sourceAccounts = Arrays.asList(
                accountRepository.findByAccountNumber(String.format("%015d", 2)).get(),
                accountRepository.findByAccountNumber(String.format("%015d", 3)).get(),
                accountRepository.findByAccountNumber(String.format("%015d", 4)).get());

        // when
        for (Account sourceAccount : sourceAccounts) {
            executorService.submit(() -> {
                try {
                    TransferRequestDTO dto = TransferRequestDTO.builder()
                            .fromAccountId(sourceAccount.getId())
                            .toAccountNumber(targetAccount.getAccountNumber())
                            .amount(transferAmount)
                            .password("1234")
                            .build();
                    transferService.executeTransfer(dto);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // then
        // 출금계좌들의 잔액 검증(각각 9000원 돼야 함)
        for (Account sourceAccount : sourceAccounts) {
            BigDecimal expectedBalance = sourceAccount.getBalance().subtract(transferAmount);
            Account updatedSourceAccount = accountRepository.findById(sourceAccount.getId()).get();
            assertEquals(
                    expectedBalance,
                    updatedSourceAccount.getBalance());
        }

        // 입금계좌의 잔액 검증(10000원 + 3000원 = 13000원 돼야 함)
        Account updatedTargetAccount = accountRepository.findById(targetAccount.getId()).get();
        assertEquals(
                initialBalance.add(transferAmount.multiply(new BigDecimal(threadCount))),
                updatedTargetAccount.getBalance());
    }

    @Test
    @Order(3)
    @DisplayName("3. 데드락 시나리오 테스트 (A<->B)")
    @Transactional
    void deadlockTest() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean deadlockDetected = new AtomicBoolean(false);

        Account accountA = accountRepository.findByAccountNumber(String.format("%015d", 1)).get();
        Account accountB = accountRepository.findByAccountNumber(String.format("%015d", 2)).get();
        BigDecimal transferAmount = new BigDecimal("1000");

        BigDecimal initialBalanceA = accountA.getBalance();
        BigDecimal initialBalanceB = accountB.getBalance();

        // when
        executorService.submit(() -> {
            try {
                TransferRequestDTO dto = TransferRequestDTO.builder()
                        .fromAccountId(accountA.getId())
                        .toAccountNumber(accountB.getAccountNumber())
                        .amount(transferAmount)
                        .password("1234")
                        .build();
                transferService.executeTransfer(dto);
            } catch (Exception e) {
                deadlockDetected.set(true);
                throw e;
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                TransferRequestDTO dto = TransferRequestDTO.builder()
                        .fromAccountId(accountB.getId())
                        .toAccountNumber(accountA.getAccountNumber())
                        .amount(transferAmount)
                        .password("1234")
                        .build();
                transferService.executeTransfer(dto);
            } catch (Exception e) {
                deadlockDetected.set(true);
                throw e;
            } finally {
                latch.countDown();
            }
        });

        // then
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executorService.shutdownNow();

        if (!completed || deadlockDetected.get()) {
            fail("데드락이 감지되었습니다.");
        }

        // 잔액이 변경되지 않았는지 확인
        Account finalAccountA = accountRepository.findById(accountA.getId()).get();
        Account finalAccountB = accountRepository.findById(accountB.getId()).get();
        assertEquals(initialBalanceA, finalAccountA.getBalance());
        assertEquals(initialBalanceB, finalAccountB.getBalance());
    }

    @Test
    @Order(4)
    @DisplayName("4. 체인 이체 테스트 (A->B->C->D)")
    @Transactional
    void chainTransferTest() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);
        AtomicBoolean deadlockDetected = new AtomicBoolean(false);

        List<Account> accounts = Arrays.asList(
                accountRepository.findByAccountNumber(String.format("%015d", 1)).get(), // A
                accountRepository.findByAccountNumber(String.format("%015d", 2)).get(), // B
                accountRepository.findByAccountNumber(String.format("%015d", 3)).get(), // C
                accountRepository.findByAccountNumber(String.format("%015d", 4)).get() // D
        );

        BigDecimal transferAmount = new BigDecimal("1000");
        Map<Long, BigDecimal> initialBalances = accounts.stream()
                .collect(Collectors.toMap(Account::getId, Account::getBalance));

        // when
        for (int i = 0; i < accounts.size(); i++) {
            final int currentIndex = i;
            executorService.submit(() -> {
                try {
                    TransferRequestDTO dto = TransferRequestDTO.builder()
                            .fromAccountId(accounts.get(currentIndex).getId())
                            .toAccountNumber(accounts.get((currentIndex + 1) % accounts.size()).getAccountNumber())
                            .amount(transferAmount)
                            .password("1234")
                            .build();
                    transferService.executeTransfer(dto);
                } catch (Exception e) {
                    deadlockDetected.set(true);
                    throw e;
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executorService.shutdownNow();

        if (!completed || deadlockDetected.get()) {
            fail("데드락이 감지되었거나 타임아웃이 발생했습니다.");
        }

        for (Account account : accounts) {
            Account updatedAccount = accountRepository.findById(account.getId()).get();
            assertEquals(initialBalances.get(account.getId()), updatedAccount.getBalance(),
                    "체인 이체 후 잔액이 일치해야 함");
        }
    }
}