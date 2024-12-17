package com.hbbank.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AutoTransfer;
import com.hbbank.backend.domain.enums.AutoTransferStatus;
import com.hbbank.backend.dto.AutoTransferExecuteDTO;
import com.hbbank.backend.dto.AutoTransferRequestDTO;
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
        log.info("자동이체 등록 시작 - 출금계좌: {}, 입금계좌: {}, 금액: {}",
                dto.getFromAccountId(), dto.getToAccountNumber(), dto.getAmount());

        Account fromAccount = accountRepository.findById(dto.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌를 찾을 수 없습니다."));
        Account toAccount = accountRepository.findByAccountNumber(dto.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("입금 계좌를 찾을 수 없습니다."));

        if (!encoder.matches(dto.getPassword(), fromAccount.getPassword())) {
            log.error("자동이체 등록 실패 - 비밀번호 불일치 (출금계좌: {})", dto.getFromAccountId());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .fromAccount(fromAccount)
                .toAccountNumber(dto.getToAccountNumber())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .transferDay(dto.getTransferDay())
                .nextTransferDate(dto.getStartDate().withDayOfMonth(dto.getTransferDay()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(AutoTransferStatus.ACTIVE)
                .failureCount(0)
                .build();

        AutoTransfer savedTransfer = autoTransferRepository.save(autoTransfer);
        log.info("자동이체 등록 완료 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                savedTransfer.getId(), dto.getFromAccountId(), dto.getToAccountNumber(), dto.getAmount());

        return savedTransfer;
    }

    // 매일 00:00 에 자동 이체 실행
    // 매일매일 해당 날짜에 startDate가 해당하는 모든 자동 이체 내역들에 대해 00:00 에 실행
    // 성능 및 정합성 테스트 필수!!!!!(대량의 자동 이체 내역 확인 필요하기 때문)
    @Scheduled(cron = "0 0 0 * * *")
    public void executeAutoTransfer() {
        LocalDate today = LocalDate.now();
        log.info("자동이체 실행 시작: {}", today);

        List<AutoTransfer> list = autoTransferRepository.findAllByNextTransferDateAndStatus(today, AutoTransferStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("리스트를 찾을 수 없습니다."));

        int totalCount = list.size();
        int successCount = 0;
        int failCount = 0;

        for (AutoTransfer at : list) {
            try {
                AutoTransferExecuteDTO dto = AutoTransferExecuteDTO.builder()
                        .fromAccountId(at.getFromAccount().getId())
                        .toAccountNumber(at.getToAccountNumber())
                        .amount(at.getAmount())
                        .build();

                transferService.executeTransfer(dto);

                at.updateNextTransferDate();
                successCount++;

                log.info("자동이체 성공 - ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                        at.getId(), at.getFromAccount().getId(), at.getToAccountNumber(), at.getAmount());

            } catch (Exception e) {
                at.increaseFailureCount();
                if (at.getFailureCount() >= 3) {
                    at.setStatusPaused();
                }

                log.error("자동이체 실패 - ID: {}, 사유: {}", at.getId(), e.getMessage());
            } finally {
                autoTransferRepository.save(at);
            }
        }

        log.info("자동이체 실행 완료 - 총 {}건 중 성공: {}건, 실패: {}건", totalCount, successCount, failCount);
    }

    // 자동 이체 만료
    @Scheduled(cron = "0 0 0 * * *")
    public void finishAutoTransfer() {
        LocalDate today = LocalDate.now();
        log.info("자동이체 만료 시작: {}", today);

        List<AutoTransfer> list = autoTransferRepository.findAllByEndDateAndStatus(today, AutoTransferStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("리스트를 찾을 수 없습니다."));

        int expiredCount = 0;

        try {
            for (AutoTransfer at : list) {
                try {
                    at.setStatusCompleted();
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
