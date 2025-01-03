package com.hbbank.backend.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hbbank.backend.domain.AutoTransfer;
import com.hbbank.backend.dto.AutoTransferRequestDTO;
import com.hbbank.backend.dto.AutoTransferResponseDTO;
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
    public ResponseEntity<AutoTransferResponseDTO> register(@Valid @RequestBody AutoTransferRequestDTO dto) {
        AutoTransfer at = autoTransferService.register(dto);
        return ResponseEntity
                .created(URI.create("/api/auto-transfer/register/" + at.getId()))
                .body(AutoTransferResponseDTO.from(at));
    }

    @GetMapping("/{autoTransferId}")
    public ResponseEntity<AutoTransferResponseDTO> findById(@PathVariable("autoTransferId") Long id) {
        return ResponseEntity
                .ok(AutoTransferResponseDTO.from(autoTransferService.findById(id)));
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<AutoTransferResponseDTO>> findAllByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(autoTransferService.findAllByUserId(userId).stream()
                        .map(AutoTransferResponseDTO::from)
                        .toList()
                );
    }

    @PutMapping("/{autoTransferId}")
    public ResponseEntity<AutoTransferResponseDTO> updateAutoTransfer(@PathVariable("autoTransferId") Long id, @Valid @RequestBody AutoTransferRequestDTO dto) {
        return ResponseEntity
                .ok(AutoTransferResponseDTO.from(autoTransferService.update(id, dto)));
    }

    @DeleteMapping("/{autoTransferId}")
    public ResponseEntity<String> deleteAutoTransfer(@PathVariable("autoTransferId") Long id) {
        autoTransferService.delete(autoTransferService.findById(id));
        return ResponseEntity
                .ok("자동이체가 성공적으로 삭제되었습니다.");
    }
}
