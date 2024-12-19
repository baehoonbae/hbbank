package com.hbbank.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<?> register(@Valid @RequestBody AutoTransferRequestDTO dto) {
        AutoTransfer t = autoTransferService.register(dto);
        if (t != null) {
            return ResponseEntity.ok(AutoTransferResponseDTO.from(t));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("자동 이체 등록 실패");
    }

    @GetMapping("/{autoTransferId}")
    public ResponseEntity<?> findById(@PathVariable("autoTransferId") Long id) {
        AutoTransfer at = autoTransferService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 자동 이체를 찾을 수 없습니다."));
                
        return ResponseEntity.ok(AutoTransferResponseDTO.from(at));
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> findAllByUserId(@PathVariable("userId") Long userId) {
        List<AutoTransfer> atlist = autoTransferService.findAllByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자동 이체 목록을 찾을 수 없습니다."));
        List<AutoTransferResponseDTO> dtos = atlist.stream()
                .map(AutoTransferResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{autoTransferId}")
    public ResponseEntity<?> updateAutoTransfer(@PathVariable("autoTransferId") Long id, @Valid @RequestBody AutoTransferRequestDTO dto) {
        AutoTransfer updated = autoTransferService.update(id, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "자동 이체 수정에 실패했습니다."));
        return ResponseEntity.ok(AutoTransferResponseDTO.from(updated));
    }

    @DeleteMapping("/{autoTransferId}")
    public ResponseEntity<?> deleteAutoTransfer(@PathVariable("autoTransferId") Long id) {
        AutoTransfer at = autoTransferService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 자동 이체입니다."));
        autoTransferService.delete(at);
        return ResponseEntity.ok("자동이체가 성공적으로 삭제되었습니다.");
    }
}
