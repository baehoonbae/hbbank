package com.hbbank.backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.dto.TransactionResponseDTO;
import com.hbbank.backend.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    // 특정 계좌 거래내역 전체 조회
    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<?> findAllByAccount_id(@PathVariable Long accountId) {
        Optional<List<Transaction>> opTransactions = transactionService.findAllByAccount_IdOrderByTransactionDateTimeDesc(accountId);

        if (opTransactions.isPresent()) {
            List<TransactionResponseDTO> dtos = opTransactions.get().stream()
                    .map(TransactionResponseDTO::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        }
        return ResponseEntity.notFound().build();
    }
}
