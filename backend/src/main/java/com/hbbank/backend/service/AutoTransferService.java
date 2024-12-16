package com.hbbank.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AutoTransfer;
import com.hbbank.backend.domain.enums.AutoTransferStatus;
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
        Account fromAccount = accountRepository.findById(dto.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌를 찾을 수 없습니다."));
        Account toAccount = accountRepository.findByAccountNumber(dto.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("입금 계좌를 찾을 수 없습니다."));

        if (!encoder.matches(dto.getPassword(), fromAccount.getPassword())) {
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

        return autoTransferRepository.save(autoTransfer);
    }

    // 매일 00:00 에 자동 이체 실행
    // 매일매일 해당 날짜에 startDate가 해당하는 모든 자동 이체 내역들에 대해 00:00 에 실행
    // 성능 및 정합성 테스트 필수!!!!!(대량의 자동 이체 내역 확인 필요하기 때문)
    @Scheduled(cron = "0 0 0 * * *")
    public void executeAutoTransfer() {
        LocalDate today = LocalDate.now();
        Optional<List<AutoTransfer>> list = autoTransferRepository.findAllByNextTransferDateAndStatus(today, AutoTransferStatus.ACTIVE);
    }

}
