package com.hbbank.backend.service;

import java.util.List;
import java.util.Optional;

import com.hbbank.backend.domain.enums.AccountStatus;
import com.hbbank.backend.exception.InvalidAccountStatusException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AccountType;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.AccountCreateDTO;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.AccountTypeRepository;
import com.hbbank.backend.repository.UserRepository;
import com.hbbank.backend.util.AccountNumberGenerator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.security.auth.login.AccountNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountNumberGenerator numGen;

    public Optional<List<AccountType>> getAccountTypes() {
        log.debug("계좌 유형 목록 조회");
        return Optional.of(accountTypeRepository.findAll());
    }

    public Account createAccount(AccountCreateDTO dto) {
        log.info("계좌 생성 시작 - 사용자ID: {}, 계좌유형: {}", dto.getUserId(), dto.getAccountTypeCode());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    log.error("계좌 생성 실패 - 사용자 없음 (사용자ID: {})", dto.getUserId());
                    return new RuntimeException("유저를 찾을 수 없습니다.");
                });

        AccountType accountType = accountTypeRepository.findById(dto.getAccountTypeCode())
                .orElseThrow(() -> {
                    log.error("계좌 생성 실패 - 계좌유형 없음 (계좌유형코드: {})", dto.getAccountTypeCode());
                    return new RuntimeException("계좌 유형을 찾을 수 없습니다.");
                });

        String accountNumber = numGen.generate(dto.getAccountTypeCode());
        log.debug("계좌번호 생성 완료: {}", accountNumber);

        Account account2 = Account.builder()
                .user(user)
                .accountType(accountType)
                .accountName(accountType.getName())
                .accountNumber(accountNumber)
                .balance(dto.getBalance())
                .interestRate(accountType.getInterestRate())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Account savedAccount = accountRepository.save(account2);
        log.info("계좌 생성 완료 - 계좌번호: {}, 계좌유형: {}, 사용자ID: {}",
                savedAccount.getAccountNumber(), savedAccount.getAccountType().getCode(), savedAccount.getUser().getId());

        return savedAccount;
    }

    public Optional<List<Account>> findAllByUser_Id(Long userId) {
        log.debug("사용자의 계좌 목록 조회 - 사용자ID: {}", userId);
        return accountRepository.findAllByUser_IdWithUser(userId);
    }

    public Optional<Account> findById(Long id) {
        log.debug("계좌 조회 - 계좌ID: {}", id);
        return accountRepository.findByIdWithUser(id);
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        log.debug("계좌번호로 계좌 조회 - 계좌번호: {}", accountNumber);
        return accountRepository.findByAccountNumberWithUser(accountNumber);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyTransferAmount() {
        log.info("일일 이체 한도 초기화 시작");
        accountRepository.resetAllDailyTransferredAmounts();
        log.info("일일 이체 한도 초기화 완료");
    }

    public void verifyAccount(Long accountId) throws Exception {
        Account a = findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("존재하지 않는 계좌입니다."));

        if (!a.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new InvalidAccountStatusException("");
        }
    }
}
