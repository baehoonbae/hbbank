package com.hbbank.backend.service;

import java.util.List;
import java.util.Optional;

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

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountNumberGenerator numGen;

    public Optional<List<AccountType>> getAccountTypes() {
        return Optional.of(accountTypeRepository.findAll());
    }

    public Account createAccount(AccountCreateDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        AccountType accountType = accountTypeRepository.findById(dto.getAccountTypeCode())
                .orElseThrow(() -> new RuntimeException("계좌 유형을 찾을 수 없습니다."));

        String accountNumber = numGen.generate(dto.getAccountTypeCode());

        Account account2 = Account.builder()
                .user(user)
                .accountType(accountType)
                .accountName(accountType.getName())
                .accountNumber(accountNumber)
                .balance(dto.getBalance())
                .interestRate(accountType.getInterestRate())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        return accountRepository.save(account2);
    }

    public Optional<List<Account>> findAllByUser_Id(Long userId) {
        return accountRepository.findAllByUser_Id(userId);
    }
}
