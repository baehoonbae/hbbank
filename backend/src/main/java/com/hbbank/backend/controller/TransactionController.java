package com.hbbank.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.dto.TransactionResponseDTO;
import com.hbbank.backend.dto.TransactionSearchDTO;
import com.hbbank.backend.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.DispatcherServlet;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    // 특정 계좌 거래내역 전체 조회
    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> findAllByAccount_id(@PathVariable("accountId") Long id) {
        return ResponseEntity.ok(transactionService.findAllByAccount_IdOrderByTransactionDateTimeDesc(id).stream()
                        .map(TransactionResponseDTO::from)
                        .toList()
                );
    }

    // 검색 조건으로 거래내역 조회
    @PostMapping("/transactions/search")
    public ResponseEntity<List<TransactionResponseDTO>> findAllByCondition(@RequestBody TransactionSearchDTO dto) {
        return ResponseEntity.ok(transactionService.findAllByCondition(dto).stream()
                        .map(TransactionResponseDTO::from)
                        .toList()
                );

    }
}
