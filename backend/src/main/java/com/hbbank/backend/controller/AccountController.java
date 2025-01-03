package com.hbbank.backend.controller;

import java.net.URI;
import java.util.List;

import com.hbbank.backend.domain.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<AccountType>> getAccountTypes() {
        return ResponseEntity
                .ok(accountService.getAccountTypes());
    }

    // 계좌 개설
    @PostMapping("/create")
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountCreateDTO request) {
        Account a = accountService.createAccount(request);

        return ResponseEntity
                .created(URI.create("/api/account/create/" + a.getId()))
                .body(AccountResponseDTO.from(a));
    }

    // 특정 유저 pk 값으로 계좌 목록 조회
    @GetMapping("/accounts/{userId}")
    public ResponseEntity<List<AccountResponseDTO>> getAccounts(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(
                        accountService.findAllByUser_Id(userId).stream()
                                .map(AccountResponseDTO::from)
                                .toList()
                );
    }

    // 계좌 pk로 특정 계좌 조회
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponseDTO> getAccount(@PathVariable("accountId") Long id) {
        return ResponseEntity
                .ok(AccountResponseDTO.from(accountService.findById(id)));
    }

    // 계좌 번호로 특정 계좌 조회
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccount(@PathVariable("accountNumber") String accountNumber) {
        return ResponseEntity
                .ok(AccountResponseDTO.from(accountService.findByAccountNumber(accountNumber)));
    }

}
