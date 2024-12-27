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
    public ResponseEntity<?> findAllByAccount_id(@PathVariable("accountId") Long id) {
        try {
            List<Transaction> t = transactionService
                    .findAllByAccount_IdOrderByTransactionDateTimeDesc(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "거래내역을 찾을 수 없습니다."));
                    
            List<TransactionResponseDTO> dtos = t.stream()
                    .map(TransactionResponseDTO::from)
                    .toList();

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 검색 조건으로 거래내역 조회
    @PostMapping("/transactions/search")
    public ResponseEntity<?> findAllByCondition(@RequestBody TransactionSearchDTO dto) {
        List<Transaction> t = transactionService.findAllByCondition(dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "거래내역을 찾을 수 없습니다."));
        List<TransactionResponseDTO> dtos = t.stream()
                .map(TransactionResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
