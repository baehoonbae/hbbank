package com.hbbank.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hbbank.backend.domain.AutoTransfer;
import com.hbbank.backend.dto.AutoTransferRequestDTO;
import com.hbbank.backend.service.AutoTransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auto-transfer")
@RequiredArgsConstructor
@Slf4j
public class AutoTransferController {

    private final AutoTransferService autoTransferService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AutoTransferRequestDTO dto) {
        AutoTransfer registeredTransfer = autoTransferService.register(dto);
        if (registeredTransfer != null) {
            return ResponseEntity.ok(registeredTransfer);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("자동 이체 등록 실패");
    }
}
