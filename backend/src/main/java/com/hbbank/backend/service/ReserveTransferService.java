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
import com.hbbank.backend.domain.enums.TransferStatus;
import com.hbbank.backend.domain.enums.TransferType;
import com.hbbank.backend.dto.ReserveTransferRequestDTO;
import com.hbbank.backend.dto.TransferRequestDTO;
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
        log.info("예약이체 등록 시작 - 출금계좌: {}, 입금계좌: {}, 금액: {}, 예약시간: {}",
                dto.getFromAccountId(), dto.getToAccountNumber(), dto.getAmount(), dto.getReservedAt());

        Account fa = accountRepository.findByIdWithUser(dto.getFromAccountId())
                .orElseThrow(() -> {
                    log.error("예약이체 등록 실패 - 출금계좌 없음 (계좌ID: {})", dto.getFromAccountId());
                    return new IllegalArgumentException("해당 계좌를 찾을 수 없습니다.");
                });

        ReserveTransfer rt = new ReserveTransfer();
        rt.update(fa, dto);
        ReserveTransfer srt = reserveTransferRepository.save(rt);

        log.info("예약이체 등록 완료 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                srt.getId(), dto.getFromAccountId(), dto.getToAccountNumber(), dto.getAmount());

        return srt;
    }

    // 특정 예약 이체 조회
    public Optional<ReserveTransfer> findById(Long id) {
        log.debug("예약이체 조회 - ID: {}", id);
        return reserveTransferRepository.findById(id);
    }

    // 예약이체 목록 조회
    public Optional<List<ReserveTransfer>> findAllByUserId(Long userId) {
        log.debug("사용자의 예약이체 목록 조회 - 사용자ID: {}", userId);
        return reserveTransferRepository.findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE);
    }

    // 예약이체 수정
    public Optional<ReserveTransfer> update(Long id, ReserveTransferRequestDTO dto) {
        log.info("예약이체 수정 시작 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                id, dto.getFromAccountId(), dto.getToAccountNumber(), dto.getAmount());

        ReserveTransfer rt = reserveTransferRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("예약이체 수정 실패 - 예약이체 없음 (ID: {})", id);
                    return new IllegalArgumentException("존재하지 않는 예약 이체입니다.");
                });

        Account fa = accountRepository.findByIdWithUser(dto.getFromAccountId())
                .orElseThrow(() -> {
                    log.error("예약이체 수정 실패 - 출금계좌 없음 (계좌ID: {})", dto.getFromAccountId());
                    return new IllegalArgumentException("출금 계좌를 찾을 수 없습니다.");
                });

        if (!encoder.matches(dto.getPassword(), fa.getPassword())) {
            log.error("예약이체 수정 실패 - 비밀번호 불일치 (ID: {})", id);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        rt.update(fa, dto);
        ReserveTransfer srt = reserveTransferRepository.save(rt);
        log.info("예약이체 수정 완료 - ID: {}", id);

        return Optional.of(srt);
    }

    // 예약이체 삭제
    public void delete(ReserveTransfer rt) {
        log.info("예약이체 삭제 - ID: {}, 출금계좌: {}, 입금계좌: {}",
                rt.getId(), rt.getFromAccount().getId(), rt.getToAccountNumber());
        reserveTransferRepository.delete(rt);
        log.info("예약이체 삭제 완료 - ID: {}", rt.getId());
    }

    // 예약 이체 실행(매 분마다)
    // 테스트 필수
    @Scheduled(cron = "0 * * * * *")
    public void executeReserveTransfers() {
        LocalDateTime now = LocalDateTime.now();
        log.info("예약이체 실행 시작: {}", now);

        List<ReserveTransfer> list = reserveTransferRepository.findAllPendingTransfers(now)
                .orElseThrow(() -> new IllegalArgumentException("리스트를 조회할 수 없습니다."));

        log.debug("예약이체 대상 조회 완료 - 총 {}건", list.size());

        int totalCount = list.size();
        int successCount = 0;
        int failCount = 0;

        for (ReserveTransfer rt : list) {
            log.debug("예약이체 실행 시도 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                    rt.getId(), rt.getFromAccount().getId(), rt.getToAccountNumber(), rt.getAmount());
            try {
                boolean success = transferService.executeTransfer(TransferRequestDTO.builder()
                        .type(TransferType.RESERVE)
                        .fromAccountId(rt.getFromAccount().getId())
                        .toAccountNumber(rt.getToAccountNumber())
                        .amount(rt.getAmount())
                        .build());

                rt.updateStatus(success);
                if (success) {
                    successCount++;
                    log.info("예약이체 성공 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                            rt.getId(), rt.getFromAccount().getId(), rt.getToAccountNumber(), rt.getAmount());
                } else {
                    failCount++;
                    log.warn("예약이체 실패 - ID: {}, 출금계좌: {}", rt.getId(), rt.getFromAccount().getId());
                }
            } catch (Exception e) {
                rt.increaseFailureCount();
                failCount++;
                log.error("예약이체 실행 중 오류 발생 - ID: {}, 출금계좌: {}, 사유: {}",
                        rt.getId(), rt.getFromAccount().getId(), e.getMessage());
            } finally {
                reserveTransferRepository.save(rt);
                log.debug("예약이체 상태 업데이트 완료 - ID: {}, 상태: {}", rt.getId(), rt.getStatus());
            }
        }
        log.info("예약이체 실행 완료 - 총 {}건 중 성공: {}건, 실패: {}건", totalCount, successCount, failCount);
    }

}
