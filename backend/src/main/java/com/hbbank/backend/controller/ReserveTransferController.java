package com.hbbank.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hbbank.backend.domain.ReserveTransfer;
import com.hbbank.backend.dto.ReserveTransferRequestDTO;
import com.hbbank.backend.dto.ReserveTransferResponseDTO;
import com.hbbank.backend.service.ReserveTransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/reserve-transfer")
@RequiredArgsConstructor
@Slf4j
public class ReserveTransferController {

    private final ReserveTransferService reserveTransferService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ReserveTransferRequestDTO dto) {
        ReserveTransfer rt = reserveTransferService.register(dto);
        if (rt != null) {
            return ResponseEntity.ok(ReserveTransferResponseDTO.from(rt));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("예약 이체 등록 실패");
    }

}
