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

    @GetMapping("/{reserveTransferId}")
    public ResponseEntity<?> findById(@PathVariable("reserveTransferId") Long id) {
        ReserveTransfer rt = reserveTransferService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 예약 이체입니다."));

        return ResponseEntity.ok(ReserveTransferResponseDTO.from(rt));
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> findAllByUserId(@PathVariable("userId") Long userId) {
        List<ReserveTransfer> list = reserveTransferService.findAllByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약 이체 목록을 찾지 못했습니다."));

        return ResponseEntity.ok(
                list.stream()
                        .map(rt -> ReserveTransferResponseDTO.from(rt))
                        .collect(Collectors.toList())
        );
    }

    @PutMapping("/{reserveTransferId}")
    public ResponseEntity<?> updateReserveTransfer(@PathVariable("reserveTransferId") Long id, @Valid @RequestBody ReserveTransferRequestDTO dto) {
        ReserveTransfer updated = reserveTransferService.update(id, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약 이체 수정에 실패했습니다."));

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{reserveTransferId}")
    public ResponseEntity<?> deleteReserveTransfer(@PathVariable("reserveTransferId") Long id) {
        ReserveTransfer rt = reserveTransferService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 예약 이체입니다."));
        reserveTransferService.delete(rt);
        return ResponseEntity.ok("예약이체가 성공적으로 삭제되었습니다.");
    }
}
