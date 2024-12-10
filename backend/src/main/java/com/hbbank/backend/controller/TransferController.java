package com.hbbank.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.service.TransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Slf4j
public class TransferController {
    private final TransferService transferService;

    @PostMapping("")
    public ResponseEntity<?> executeTransfer(@Valid @RequestBody TransferRequestDTO dto) {
        boolean success = transferService.executeTransfer(dto);
        if (success) {
            return ResponseEntity.ok(success);
        }
        return ResponseEntity.badRequest().body("이체 실패");
    }

}
