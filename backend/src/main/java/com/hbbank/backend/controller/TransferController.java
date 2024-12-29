package com.hbbank.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequestDTO dto) {
        boolean success = transferService.transfer(dto);
        if (success) {
            return ResponseEntity.ok(success);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이체에 실패하였습니다.");
    }

}
