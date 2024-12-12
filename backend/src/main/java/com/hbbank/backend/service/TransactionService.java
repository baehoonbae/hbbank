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
        return transactionRepository.findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);
    }

    public Optional<List<Transaction>> findAllByCondition(TransactionSearchDTO dto) {
        return transactionRepository.findAllByCondition(dto);
    }

}
