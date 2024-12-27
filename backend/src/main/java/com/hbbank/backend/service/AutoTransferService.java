package com.hbbank.backend.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AutoTransfer;
import com.hbbank.backend.domain.enums.TransferStatus;
import com.hbbank.backend.domain.enums.TransferType;
import com.hbbank.backend.dto.AutoTransferRequestDTO;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.AutoTransferRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AutoTransferService {

    private final AccountRepository accountRepository;
    private final AutoTransferRepository autoTransferRepository;
    private final TransferService transferService;
    private final PasswordEncoder encoder;

    // 자동 이체 등록
    public AutoTransfer register(AutoTransferRequestDTO dto) {
        log.info("자동이체 등록 시작 - 출금계좌: {}, 입금계좌: {}, 금액: {}, 이체일: {}, 시작일: {}, 종료일: {}",
                dto.getFromAccountId(), dto.getToAccountNumber(), dto.getAmount(),
                dto.getTransferDay(), dto.getStartDate(), dto.getEndDate());

        dto.validate();
        
        Account fa = accountRepository.findById(dto.getFromAccountId())
                .orElseThrow(() -> {
                    log.error("자동이체 등록 실패 - 출금계좌 없음 (계좌ID: {})", dto.getFromAccountId());
                    return new IllegalArgumentException("출금 계좌를 찾을 수 없습니다.");
                });

        if (!encoder.matches(dto.getPassword(), fa.getPassword())) {
            log.error("자동이체 등록 실패 - 비밀번호 불일치 (출금계좌: {})", dto.getFromAccountId());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        AutoTransfer at = new AutoTransfer();
        at.update(fa, dto);

        AutoTransfer sat = autoTransferRepository.save(at);
        log.info("자동이체 등록 완료 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}, 이체일: {}",
                sat.getId(), 
                dto.getFromAccountId(), 
                dto.getToAccountNumber(),
                dto.getAmount(), 
                dto.getTransferDay());

        return sat;
    }

    // 자동 이체 조회
    public Optional<AutoTransfer> findById(Long autoTransferId) {
        return autoTransferRepository.findById(autoTransferId);
    }

    // 자동 이체 목록 조회
    public Optional<List<AutoTransfer>> findAllByUserId(Long userId) {
        return autoTransferRepository.findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE);
    }

    // 자동 이체 수정
    public Optional<AutoTransfer> update(Long id, AutoTransferRequestDTO dto) {
        log.info("자동이체 수정 시작 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}, 이체일: {}",
                id, dto.getFromAccountId(), dto.getToAccountNumber(), dto.getAmount(), dto.getTransferDay());

        AutoTransfer autoTransfer = autoTransferRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("자동이체 수정 실패 - 자동이체 없음 (ID: {})", id);
                    return new IllegalArgumentException("해당 자동이체를 찾을 수 없습니다.");
                });

        Account fromAccount = accountRepository.findByIdWithUser(dto.getFromAccountId())
                .orElseThrow(() -> {
                    log.error("자동이체 수정 실패 - 출금계좌 없음 (계좌ID: {})", dto.getFromAccountId());
                    return new IllegalArgumentException("출금 계좌를 찾을 수 없습니다.");
                });

        if (!encoder.matches(dto.getPassword(), fromAccount.getPassword())) {
            log.error("자동이체 수정 실패 - 비밀번호 불일치 (출금계좌: {})", dto.getFromAccountId());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        autoTransfer.update(fromAccount, dto);
        AutoTransfer updated = autoTransferRepository.save(autoTransfer);

        log.info("자동이체 수정 완료 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}, 이체일: {}",
                updated.getId(), 
                updated.getFromAccount().getId(), 
                updated.getToAccountNumber(),
                updated.getAmount(), 
                updated.getTransferDay());

        return Optional.of(updated);
    }

    // 자동 이체 삭제
    public void delete(AutoTransfer at) {
        log.info("자동이체 삭제 - ID: {}, 출금계좌: {}, 입금계좌: {}",
                at.getId(), at.getFromAccount().getId(), at.getToAccountNumber());
        autoTransferRepository.delete(at);
        log.info("자동이체 삭제 완료 - ID: {}", at.getId());
    }

    // 자동 이체 실행(매일 00:00)
    // 매일매일 해당 날짜에 startDate가 해당하는 모든 자동 이체 내역들에 대해 00:00 에 실행
    // 성능 및 정합성 테스트 필수!!!!!(대량의 자동 이체 내역 확인 필요하기 때문)
    @Scheduled(cron = "0 0 0 * * *")
    public void executeAutoTransfer() {
        LocalDate today = LocalDate.now();
        log.info("자동이체 실행 시작: {}", today);

        List<AutoTransfer> list = autoTransferRepository
                .findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE)
                .orElse(Collections.emptyList());

        int totalCount = list.size();
        int successCount = 0;
        int failCount = 0;

        for (AutoTransfer at : list) {
            try {
                boolean success = transferService.transfer(TransferRequestDTO.builder()
                        .type(TransferType.AUTO)
                        .fromAccountId(at.getFromAccount().getId())
                        .toAccountNumber(at.getToAccountNumber())
                        .amount(at.getAmount())
                        .build());
                at.updateStatus(success);
                if (success) {
                    successCount++;
                    log.info("자동이체 성공 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                            at.getId(), at.getFromAccount().getId(), at.getToAccountNumber(), at.getAmount());
                } else {
                    failCount++;
                    log.warn("자동이체 실패 - ID: {}, 출금계좌: {}", at.getId(), at.getFromAccount().getId());
                }
            } catch (Exception e) {
                at.increaseFailureCount();
                failCount++;
                log.error("자동이체 실행 중 오류 발생 - ID: {}, 출금계좌: {}, 사유: {}",
                        at.getId(), at.getFromAccount().getId(), e.getMessage());
            } finally {
                autoTransferRepository.save(at);
            }
        }
        log.info("자동이체 실행 완료 - 총 {}건 중 성공: {}건, 실패: {}건", totalCount, successCount, failCount);
    }

    // 자동 이체 만료(매일 00:00)
    // 마찬가지로 성능/정합성 테스트 필수
    @Scheduled(cron = "0 0 0 * * *")
    public void finishAutoTransfer() {
        LocalDate today = LocalDate.now();
        log.info("자동이체 만료 시작: {}", today);

        List<AutoTransfer> list = autoTransferRepository.findAllByEndDateAndStatus(today, TransferStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("리스트를 찾을 수 없습니다."));

        int expiredCount = 0;

        try {
            for (AutoTransfer at : list) {
                try {
                    at.setStatus(TransferStatus.COMPLETED);
                    autoTransferRepository.save(at);
                    expiredCount++;

                    log.info("자동이체 만료 처리 완료 - ID: {}, 출금계좌: {}",
                            at.getId(), at.getFromAccount().getId());
                } catch (Exception e) {
                    log.error("자동이체 만료 처리 실패 - ID: {}, 사유: {}",
                            at.getId(), e.getMessage());
                }
            }
        } finally {
            log.info("자동이체 만료 처리 완료 - 총 {}건 만료됨", expiredCount);
        }
    }
}
