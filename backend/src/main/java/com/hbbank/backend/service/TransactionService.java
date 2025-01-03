package com.hbbank.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.hbbank.backend.domain.enums.TransferType;
import com.hbbank.backend.exception.transaction.InvalidDateRangeException;
import org.springframework.stereotype.Service;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.dto.TransactionSearchDTO;
import com.hbbank.backend.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public List<Transaction> findByAccountAndTransactionType(Account account, String type) {
        return transactionRepository.findByAccountAndTransactionType(account, type)
                .orElse(Collections.emptyList());
    }

    public List<Transaction> createTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
        List<Transaction> list = new ArrayList<>();
        list.add(createWithdrawTransaction(fromAccount, toAccount, amount));
        list.add(createDepositTransaction(fromAccount, toAccount, amount));
        return list;
    }

    /* 출금 거래내역 생성 */
    public Transaction createWithdrawTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
        log.info("출금 거래내역 생성 - 출금계좌: {}, 입금계좌: {}, 금액: {}, 잔액: {}",
                fromAccount.getId(), toAccount.getId(), amount, fromAccount.getBalance());

        Transaction withdraw = Transaction.builder()
                .account(fromAccount)
                .transactionDateTime(LocalDateTime.now())
                .transactionType("출금")
                .sender(fromAccount.getUser().getName())
                .receiver(toAccount.getUser().getName())
                .withdrawalAmount(amount)
                .depositAmount(BigDecimal.ZERO)
                .balance(fromAccount.getBalance())
                .build();

        return transactionRepository.saveAndFlush(withdraw);
    }

    /* 입금 거래내역 생성 */
    public Transaction createDepositTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
        log.info("입금 거래내역 생성 - 출금계좌: {}, 입금계좌: {}, 금액: {}, 잔액: {}",
                fromAccount.getId(), toAccount.getId(), amount, toAccount.getBalance());

        Transaction deposit = Transaction.builder()
                .account(toAccount)
                .transactionDateTime(LocalDateTime.now())
                .transactionType("입금")
                .sender(fromAccount.getUser().getName())
                .receiver(toAccount.getUser().getName())
                .withdrawalAmount(BigDecimal.ZERO)
                .depositAmount(amount)
                .balance(toAccount.getBalance())
                .build();

        return transactionRepository.saveAndFlush(deposit);
    }

    /* 계좌 ID에 따라 거래내역을 모두 가져오고 거래 일시를 기준으로 내림차순 정렬 */
    public List<Transaction> findAllByAccount_IdOrderByTransactionDateTimeDesc(Long accountId) {
        accountService.verifyAccount(accountId);

        List<Transaction> transactions = transactionRepository
                .findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId)
                .orElse(Collections.emptyList());

        log.info("계좌 거래내역 조회 - 계좌ID: {}, 조회결과: {} 건", accountId, transactions.size());

        return transactions;
    }

    /* 거래내역을 특정 조건에 따라 조회 */
    public List<Transaction> findAllByCondition(TransactionSearchDTO dto) {
        if (dto.getAccountId() != null && dto.getAccountId() > 0) {
            accountService.verifyAccount(dto.getAccountId());
        }
        if (LocalDate.parse(dto.getStartDate()).isAfter(LocalDate.parse(dto.getEndDate()))) {
            throw new InvalidDateRangeException("종료일이 시작일보다 앞설 수 없습니다.");
        }
        List<Transaction> transactions = transactionRepository
                .findAllByCondition(dto)
                .orElse(Collections.emptyList());

        log.info("거래내역 조건 조회 - 조회조건: {}, 조회결과: {} 건", dto, transactions.size());

        return transactions;
    }

}
