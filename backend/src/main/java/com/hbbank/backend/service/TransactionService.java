package com.hbbank.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.hbbank.backend.domain.Account;
import org.springframework.stereotype.Service;

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
        log.debug("거래내역 생성 시작 - 출금계좌: {}, 입금계좌: {}, 금액: {}",
                fromAccount.getId(), toAccount.getId(), amount);

        log.debug("출금 거래내역 생성");
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

        log.debug("거래내역 저장 시작");
        transactionRepository.save(withdraw);
        transactionRepository.flush();
        log.debug("거래내역 저장 완료");

        log.debug("거래내역 생성 완료 - 출금계좌: {}, 입금계좌: {}, 금액: {}",
                fromAccount.getId(), toAccount.getId(), amount);
    }

    /*
    입금 거래내역 생성
     */
    public void createDepositTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
        log.debug("입금 거래내역 생성 시작 - 입금계좌: {}, 금액: {}",
                toAccount.getId(), amount);

        log.debug("입금 거래내역 생성");
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

        log.debug("입금 거래내역 저장 시작");
        transactionRepository.save(deposit);
        transactionRepository.flush();
        log.debug("입금 거래내역 저장 완료");

        log.debug("입금 거래내역 생성 완료 - 입금계좌: {}, 금액: {}",
                toAccount.getId(), amount);
    }

    /*
    계좌 ID에 따라 거래내역을 모두 가져오고 거래 일시를 기준으로 내림차순 정렬
     */
    public Optional<List<Transaction>> findAllByAccount_IdOrderByTransactionDateTimeDesc(Long accountId) throws Exception {
        log.debug("계좌 검증 시작 - 계좌ID: {}", accountId);
        accountService.verifyAccount(accountId);
        log.debug("계좌 검증 종료 - 계좌ID: {}", accountId);

        log.debug("계좌 거래내역 조회 시작 - 계좌ID: {}", accountId);
        Optional<List<Transaction>> transactions = transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);
        log.debug("계좌 거래내역 조회 완료 - 계좌ID: {}, 조회결과: {} 건", accountId,
                transactions.map(List::size).orElse(0));

        return transactions;
    }

    /*
    거래내역을 특정 조건에 따라 조회
     */
    public Optional<List<Transaction>> findAllByCondition(TransactionSearchDTO dto) {
        log.debug("거래내역 조건 조회 시작 - 조회조건: {}", dto);
        Optional<List<Transaction>> transactions = transactionRepository.findAllByCondition(dto);
        log.debug("거래내역 조건 조회 완료 - 조회결과: {} 건",
                transactions.map(List::size).orElse(0));
        return transactions;
    }

}
