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

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AccountType;
import com.hbbank.backend.dto.AccountCreateDTO;
import com.hbbank.backend.dto.AccountResponseDTO;
import com.hbbank.backend.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // 모든 계좌 타입 조회
    @GetMapping("/account-types")
    public ResponseEntity<?> getAccountTypes() {
        List<AccountType> accountTypes = accountService.getAccountTypes()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계좌 타입 목록을 찾을 수 없습니다."));
        return ResponseEntity.ok(accountTypes);
    }

    // 계좌 개설
    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreateDTO request) {
        Account a = accountService.createAccount(request);
        if (a != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(a);
        }
        return ResponseEntity.badRequest().body("계좌 개설 실패");
    }

    // 특정 유저 pk 값으로 계좌 목록 조회
    @GetMapping("/accounts/{userId}")
    public ResponseEntity<?> getAccounts(@PathVariable Long userId) {
        List<Account> list = accountService.findAllByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계좌 목록을 찾을 수 없습니다."));

        List<AccountResponseDTO> dtos = list.stream()
                .map(AccountResponseDTO::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 계좌 pk로 특정 계좌 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccount(@PathVariable Long id) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다."));

        AccountResponseDTO accountDTO = AccountResponseDTO.from(account);
        return ResponseEntity.ok(accountDTO);
    }

    // 계좌 번호로 특정 계좌 조회
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다.")); 

        AccountResponseDTO accountDTO = AccountResponseDTO.from(account);
        return ResponseEntity.ok(accountDTO);
    }

}
