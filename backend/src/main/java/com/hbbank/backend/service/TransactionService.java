package com.hbbank.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public void createTransaction(Account fromAccount, Account toAccount, BigDecimal amount){
        createWithdrawTransaction(fromAccount, toAccount, amount);
        createDepositTransaction(fromAccount, toAccount, amount);
    }

    /*
    출금 거래내역 생성
     */
    public void createWithdrawTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
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

        transactionRepository.save(withdraw);
        transactionRepository.flush();
    }

    /*
    입금 거래내역 생성
     */
    public void createDepositTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
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

        transactionRepository.save(deposit);
        transactionRepository.flush();
    }

    /*
    계좌 ID에 따라 거래내역을 모두 가져오고 거래 일시를 기준으로 내림차순 정렬
     */
    public Optional<List<Transaction>> findAllByAccount_IdOrderByTransactionDateTimeDesc(Long accountId) throws Exception {
        accountService.verifyAccount(accountId);
        Optional<List<Transaction>> transactions = transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);
        log.info("계좌 거래내역 조회 - 계좌ID: {}, 조회결과: {} 건", accountId,
                transactions.map(List::size).orElse(0));
        return transactions;
    }

    /*
    거래내역을 특정 조건에 따라 조회
     */
    public Optional<List<Transaction>> findAllByCondition(TransactionSearchDTO dto) {
        Optional<List<Transaction>> transactions = transactionRepository.findAllByCondition(dto);
        log.info("거래내역 조건 조회 - 조회조건: {}, 조회결과: {} 건", 
                dto, transactions.map(List::size).orElse(0));
        return transactions;
    }

}
