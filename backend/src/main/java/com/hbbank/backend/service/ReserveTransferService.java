package com.hbbank.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.ReserveTransfer;
import com.hbbank.backend.domain.enums.ReserveTransferStatus;
import com.hbbank.backend.dto.ReserveTransferRequestDTO;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.ReserveTransferRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReserveTransferService {

    private final ReserveTransferRepository reserveTransferRepository;
    private final AccountRepository accountRepository;

    // 예약이체 등록
    public ReserveTransfer register(ReserveTransferRequestDTO dto) {
        Account fa = accountRepository.findByIdWithUser(dto.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌를 찾을 수 없습니다."));

        ReserveTransfer rt = ReserveTransfer.builder()
                .user(fa.getUser())
                .fromAccount(fa)
                .toAccountNumber(dto.getToAccountNumber())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .reservedAt(dto.getReservedAt())
                .status(ReserveTransferStatus.PENDING)
                .failureCount(0)
                .build();

        return reserveTransferRepository.save(rt);
    }

}
