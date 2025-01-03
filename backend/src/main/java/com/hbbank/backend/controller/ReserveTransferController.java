package com.hbbank.backend.controller;

import java.net.URI;
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
    public ResponseEntity<ReserveTransferResponseDTO> register(@Valid @RequestBody ReserveTransferRequestDTO dto) {
        ReserveTransfer rt = reserveTransferService.register(dto);
        return ResponseEntity
                .created(URI.create("/api/reserve-transfer/register/" + rt.getId()))
                .body(ReserveTransferResponseDTO.from(rt));
    }

    @GetMapping("/{reserveTransferId}")
    public ResponseEntity<ReserveTransferResponseDTO> findById(@PathVariable("reserveTransferId") Long id) {
        return ResponseEntity
                .ok(ReserveTransferResponseDTO.from(reserveTransferService.findById(id)));
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<ReserveTransferResponseDTO>> findAllByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(reserveTransferService.findAllByUserId(userId).stream()
                        .map(ReserveTransferResponseDTO::from)
                        .toList()
                );
    }

    @PutMapping("/{reserveTransferId}")
    public ResponseEntity<ReserveTransferResponseDTO> updateReserveTransfer(@PathVariable("reserveTransferId") Long id, @Valid @RequestBody ReserveTransferRequestDTO dto) {
        return ResponseEntity
                .ok(ReserveTransferResponseDTO.from(reserveTransferService.update(id, dto)));
    }

    @DeleteMapping("/{reserveTransferId}")
    public ResponseEntity<String> deleteReserveTransfer(@PathVariable("reserveTransferId") Long id) {
        reserveTransferService.delete(reserveTransferService.findById(id));
        return ResponseEntity
                .ok("예약이체가 성공적으로 삭제되었습니다.");
    }
}
