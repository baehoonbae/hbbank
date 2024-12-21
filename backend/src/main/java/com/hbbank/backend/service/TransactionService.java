package com.hbbank.backend.service;

import java.util.List;
import java.util.Optional;

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

    public Optional<List<Transaction>> findAllByAccount_IdOrderByTransactionDateTimeDesc(Long accountId) {
        log.debug("계좌 거래내역 조회 시작 - 계좌ID: {}", accountId);
        Optional<List<Transaction>> transactions = transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);
        log.debug("계좌 거래내역 조회 완료 - 계좌ID: {}, 조회결과: {} 건", accountId, 
            transactions.map(List::size).orElse(0));
        return transactions;
    }

    public Optional<List<Transaction>> findAllByCondition(TransactionSearchDTO dto) {
        log.debug("거래내역 조건 조회 시작 - 조회조건: {}", dto);
        Optional<List<Transaction>> transactions = transactionRepository.findAllByCondition(dto);
        log.debug("거래내역 조건 조회 완료 - 조회결과: {} 건", 
            transactions.map(List::size).orElse(0));
        return transactions;
    }

}
