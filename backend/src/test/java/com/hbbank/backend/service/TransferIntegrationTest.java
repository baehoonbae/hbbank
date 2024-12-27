package com.hbbank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.hbbank.backend.config.TestConfig;
import com.hbbank.backend.config.TestDataConfig;
import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.domain.enums.TransferType;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.TransactionRepository;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

// 이체 관련 동시성 시나리오 통합 테스트
@Import({TestDataConfig.class, TestConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @RequiredArgsConstructor // 스프링 테스트 클래스는 다른 생명주기를 가진다.. Autowired 직접 달기
@ActiveProfiles("test")
@Slf4j
class TransferIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestDataConfig testDataConfig;

    private void verifyTransferResult(
            Account fromAccount, // 출금 계좌
            Account toAccount, // 입금 계좌
            BigDecimal transferAmount // 이체 금액
    ) {
        // 거래 내역 상세 검증
        List<Transaction> withdrawals = transactionRepository
                .findByAccountAndTransactionType(fromAccount, "출금")
                .get()
                .stream()
                .sorted((a, b) -> b.getTransactionDateTime().compareTo(a.getTransactionDateTime()))
                .collect(Collectors.toList());

        Transaction withdrawal = withdrawals.stream()
                .filter(t -> t.getWithdrawalAmount().stripTrailingZeros().equals(transferAmount.stripTrailingZeros()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("출금 거래내역을 찾을 수 없습니다"));

        // 출금 거래내역 검증
        assertEquals("출금", withdrawal.getTransactionType());
        assertEquals(fromAccount.getUser().getName(), withdrawal.getSender());
        assertEquals(toAccount.getUser().getName(), withdrawal.getReceiver());
        assertEquals(transferAmount.stripTrailingZeros(), withdrawal.getWithdrawalAmount().stripTrailingZeros());
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), withdrawal.getDepositAmount().stripTrailingZeros());

        // 입금 거래내역 검증
        List<Transaction> deposits = transactionRepository
                .findByAccountAndTransactionType(toAccount, "입금").get();

        Transaction deposit = deposits.stream()
                .filter(t -> t.getDepositAmount().stripTrailingZeros().equals(transferAmount.stripTrailingZeros()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("입금 거래내역을 찾을 수 없습니다"));

        assertEquals("입금", deposit.getTransactionType());
        assertEquals(fromAccount.getUser().getName(), deposit.getSender());
        assertEquals(toAccount.getUser().getName(), deposit.getReceiver());
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), deposit.getWithdrawalAmount().stripTrailingZeros());
        assertEquals(transferAmount.stripTrailingZeros(), deposit.getDepositAmount().stripTrailingZeros());
    }

    @BeforeAll
    void setup() {
        // 스프링 컨텍스트가 완전히 초기화된 후에 실행되도록 보장
        try {
            testDataConfig.cleanupBean().run();
            testDataConfig.init().run();
        } catch (Exception e) {
            throw new RuntimeException("테스트 데이터 초기화 실패", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 기본 순차적인 이체 테스트")
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
                .type(TransferType.INSTANT)
                .fromAccountId(fromAccount.getId())
                .toAccountNumber(toAccount.getAccountNumber())
                .amount(transferAmount)
                .password("1234")
                .build();

        transferService.transfer(dto);

        // then
        Account updatedFromAccount = accountRepository.findByIdWithUser(fromAccount.getId()).get();
        Account updatedToAccount = accountRepository.findByIdWithUser(toAccount.getId()).get();

        assertEquals(fromInitialBalance.subtract(transferAmount).stripTrailingZeros(),
                updatedFromAccount.getBalance().stripTrailingZeros());
        assertEquals(toInitialBalance.add(transferAmount).stripTrailingZeros(),
                updatedToAccount.getBalance().stripTrailingZeros());
        verifyTransferResult(updatedFromAccount, updatedToAccount, transferAmount);
    }

    @Test
    @Order(2)
    @DisplayName("2. 동시 다중 이체 테스트 (B,C,D -> A)")
    void concurrentMultipleTransferTest() throws InterruptedException {
        // given
        int threadCount = 3; // B, C, D 세 계좌에서 동시 이체
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Account targetAccount = accountRepository.findByAccountNumberWithUser(String.format("%015d", 1)).get();
        BigDecimal initialBalance = targetAccount.getBalance();
        BigDecimal transferAmount = new BigDecimal("1000");

        List<Account> fromAccounts = Arrays.asList(
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 2)).get(),
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 3)).get(),
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 4)).get());

        // when
        for (Account sourceAccount : fromAccounts) {
            executorService.submit(() -> {
                try {
                    TransferRequestDTO dto = TransferRequestDTO.builder()
                            .type(TransferType.INSTANT)
                            .fromAccountId(sourceAccount.getId())
                            .toAccountNumber(targetAccount.getAccountNumber())
                            .amount(transferAmount)
                            .password("1234")
                            .build();
                    transferService.transfer(dto);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Awaitility를 사용하여 트랜잭션 완료 대기
       await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Account account = accountRepository.findByIdWithUser(targetAccount.getId()).get();
                    assertNotNull(account);
                });

        // then
        // 출금계좌들의 잔액 검증(각각 9000원 돼야 함)
        for (Account a : fromAccounts) {
            BigDecimal expectedBalance = a.getBalance().subtract(transferAmount);
            BigDecimal actualBalance = accountRepository.findByIdWithUser(a.getId()).get().getBalance();
            assertEquals(expectedBalance, actualBalance);
        }

        // 입금계좌의 잔액 검증(10000원 + 3000원 = 13000원 돼야 함)
        Account updatedTargetAccount = accountRepository.findByIdWithUser(targetAccount.getId()).get();
        BigDecimal expectedBalance = initialBalance.add(transferAmount.multiply(new BigDecimal(threadCount)));
        BigDecimal actualBalance = updatedTargetAccount.getBalance();
        assertEquals(expectedBalance, actualBalance);

        List<Account> updatedAccounts = Arrays.asList(
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 2)).get(),
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 3)).get(),
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 4)).get());

        // 거래내역 생성 확인
        for (Account updatedFromAccount : updatedAccounts) {
            verifyTransferResult(updatedFromAccount, updatedTargetAccount, transferAmount);
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 데드락 시나리오 테스트 (A<->B)")
    void deadlockTest() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(2);
        AtomicBoolean deadlockDetected = new AtomicBoolean(false);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        transactionTemplate.setTimeout(3);

        Account accountA = accountRepository.findByAccountNumberWithUser(String.format("%015d", 1)).get();
        Account accountB = accountRepository.findByAccountNumberWithUser(String.format("%015d", 2)).get();
        BigDecimal transferAmount = new BigDecimal("1000");

        Map<Long, BigDecimal> initialBalances = Map.of(
                accountA.getId(), accountA.getBalance(),
                accountB.getId(), accountB.getBalance());

        // when
        // A->B 이체
        executorService.submit(() -> {
            try {
                startLatch.await();
                boolean success = transactionTemplate.execute(status -> {
                    try {
                        TransferRequestDTO dto = TransferRequestDTO.builder()
                                .type(TransferType.INSTANT)
                                .fromAccountId(accountA.getId())
                                .toAccountNumber(accountB.getAccountNumber())
                                .amount(transferAmount)
                                .password("1234")
                                .build();
                        return transferService.transfer(dto);
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        exceptions.add(e);
                        throw e;
                    }
                });
                if (success) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                deadlockDetected.set(true);
            } finally {
                completionLatch.countDown();
            }
        });

        // B->A 이체
        executorService.submit(() -> {
            try {
                startLatch.await();
                Thread.sleep(50); // B->A 이체를 조금 더 늦게 시작
                boolean success = transactionTemplate.execute(status -> {
                    try {
                        TransferRequestDTO dto = TransferRequestDTO.builder()
                                .fromAccountId(accountB.getId())
                                .toAccountNumber(accountA.getAccountNumber())
                                .amount(transferAmount)
                                .password("1234")
                                .build();
                        return transferService.transfer(dto);
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        exceptions.add(e);
                        throw e;
                    }
                });
                if (success) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                deadlockDetected.set(true);
            } finally {
                completionLatch.countDown();
            }
        });

        startLatch.countDown();

        // then
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS); // 타임아웃 시간 증가
        executorService.shutdownNow();

        if (!exceptions.isEmpty()) {
            exceptions.forEach(e -> log.error(e.getMessage(), e));
        }

        if (!completed || deadlockDetected.get()) {
            fail("데드락이 감지되었거나 타임아웃이 발생했습니다.");
        }

        if (successCount.get() != 2) {
            fail("두 이체 작업이 모두 성공하지 않았습니다. 성공 횟수: " + successCount.get());
        }

        // 트랜잭션이 완전히 커밋될 때까지 잠시 대기
        Thread.sleep(2000); // 대기 시간 증가

        // 잔액 검증 (원래 잔액과 동일해야 함)
        entityManager.clear(); // 영속성 컨텍스트 초기화
        Account updatedAccountA = accountRepository.findByIdWithUser(accountA.getId()).get();
        Account updatedAccountB = accountRepository.findByIdWithUser(accountB.getId()).get();

        assertEquals(initialBalances.get(accountA.getId()), updatedAccountA.getBalance(),
                "A 계좌의 최종 잔액이 초기 잔액과 일치해야 함");
        assertEquals(initialBalances.get(accountB.getId()), updatedAccountB.getBalance(),
                "B 계좌의 최종 잔액이 초기 잔액과 일치해야 함");

        // 거래내역 생성 확인
        verifyTransferResult(updatedAccountA, updatedAccountB, transferAmount);
        verifyTransferResult(updatedAccountB, updatedAccountA, transferAmount);
    }

    @Test
    @Order(4)
    @DisplayName("4. 체인 이체 테스트 (A->B->C->D->A)")
    void chainTransferTest() throws InterruptedException {
        // given
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(4);
        AtomicBoolean deadlockDetected = new AtomicBoolean(false);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        List<Account> accounts = Arrays.asList(
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 1)).get(),
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 2)).get(),
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 3)).get(),
                accountRepository.findByAccountNumberWithUser(String.format("%015d", 4)).get());

        BigDecimal transferAmount = new BigDecimal("1000");
        Map<Long, BigDecimal> initialBalances = accounts.stream()
                .collect(Collectors.toMap(Account::getId, Account::getBalance));

        // when
        for (int i = 0; i < accounts.size(); i++) {
            final int currentIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    TransferRequestDTO dto = TransferRequestDTO.builder()
                            .type(TransferType.INSTANT)
                            .fromAccountId(accounts.get(currentIndex).getId())
                            .toAccountNumber(accounts.get((currentIndex + 1) % accounts.size()).getAccountNumber())
                            .amount(transferAmount)
                            .password("1234")
                            .build();

                    // 재시도 로직 추가
                    int retryCount = 0;
                    boolean success = false;
                    while (!success && retryCount < 3) {
                        try {
                            success = transactionTemplate.execute(status -> {
                                try {
                                    return transferService.transfer(dto);
                                } catch (Exception e) {
                                    status.setRollbackOnly();
                                    throw e;
                                }
                            });
                            if (success) {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            retryCount++;
                            if (retryCount >= 3) {
                                exceptions.add(e);
                                deadlockDetected.set(true);
                            }
                            Thread.sleep(100); // 재시도 전 잠시 대기
                        }
                    }
                } catch (Exception e) {
                    deadlockDetected.set(true);
                    exceptions.add(e);
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        // then
        boolean completed = completionLatch.await(15, TimeUnit.SECONDS); // 타임아웃 시간 증가
        executorService.shutdownNow();

        if (!exceptions.isEmpty()) {
            exceptions.forEach(e -> log.error(e.getMessage(), e));
        }

        if (!completed || deadlockDetected.get()) {
            fail("데드락이 감지되었거나 타임아웃이 발생했습니다.");
        }

        if (successCount.get() != 4) {
            fail("모든 이체 작업이 성공하지 않았습니다. 성공 횟수: " + successCount.get());
        }

        // 트랜잭션이 완전히 커밋될 때까지 충분히 대기
        Thread.sleep(3000);

        // 잔액 검증
        entityManager.clear();
        for (Account account : accounts) {
            Account updatedAccount = accountRepository.findByIdWithUser(account.getId()).get();
            assertEquals(initialBalances.get(account.getId()), updatedAccount.getBalance(),
                    String.format("%d번 계좌의 최종 잔액이 초기 잔액과 일치해야 함", account.getId()));
        }

        // 거래내역 검증
        for (int i = 0; i < accounts.size(); i++) {
            verifyTransferResult(accounts.get(i), accounts.get((i + 1) % accounts.size()), transferAmount);
        }
    }

}
