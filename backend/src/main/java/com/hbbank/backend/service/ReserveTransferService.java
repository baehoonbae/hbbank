package com.hbbank.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final TransferService transferService;
    private final PasswordEncoder encoder;

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

    // 특정 예약 이체 조회
    public Optional<ReserveTransfer> findById(Long id) {
        return reserveTransferRepository.findById(id);
    }

    // 예약이체 목록 조회
    public Optional<List<ReserveTransfer>> findAllByUserId(Long userId) {
        return reserveTransferRepository.findAllByUserIdAndStatus(userId, ReserveTransferStatus.PENDING);
    }

    // 예약이체 수정
    public Optional<ReserveTransfer> update(Long id, ReserveTransferRequestDTO dto) {
        ReserveTransfer rt = reserveTransferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 이체입니다."));

        Account fromAccount = accountRepository.findByIdWithUser(dto.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌를 찾을 수 없습니다."));

        if (!encoder.matches(dto.getPassword(), fromAccount.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        rt.update(fromAccount, dto);

        return Optional.of(reserveTransferRepository.save(rt));
    }

    // 예약이체 삭제
    public void delete(ReserveTransfer rt) {
        reserveTransferRepository.delete(rt);
    }

    // 예약 이체 실행(매 분마다)
    @Scheduled(cron = "0 * * * * *")
    public void executeReserveTransfers() {
        // 현재 시각 이전의 PENDING 상태인 예약 이체들을 조회
        List<ReserveTransfer> pendingList = reserveTransferRepository.findAllPendingTransfers(LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("리스트를 조회할 수 없습니다."));

        for(ReserveTransfer rt:pendingList){
            // 이체 실행
            
            transferService.executeTransfer();
        }
    }

}
