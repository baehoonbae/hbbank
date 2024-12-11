package com.hbbank.backend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.env.Environment;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AccountType;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.AccountTypeRepository;
import com.hbbank.backend.repository.RefreshTokenRepository;
import com.hbbank.backend.repository.TransactionRepository;
import com.hbbank.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TestConfiguration
@RequiredArgsConstructor
@Slf4j
public class TestDataConfig {

    private final int ACCOUNT_NUMBER = 30;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final TransactionRepository transactionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationContext applicationContext;

    @Bean
    @Order(1)
    @Profile("test")
    public CommandLineRunner cleanupBean() {
        return args -> {
            String jdbcUrl = applicationContext.getEnvironment()
                .getProperty("spring.datasource.url");
            
            if (jdbcUrl == null || !jdbcUrl.contains("hbbank_test")) {
                throw new IllegalStateException("테스트는 반드시 hbbank_test 데이터베이스에서 실행되어야 합니다. 현재 URL: " + jdbcUrl);
            }
            try{
                log.info("테스트 데이터베이스 초기화 시작: {}", jdbcUrl);
                refreshTokenRepository.deleteAll();
                transactionRepository.deleteAll();
                accountRepository.deleteAll();
                userRepository.deleteAll();
                accountTypeRepository.deleteAll();
                log.info("테스트 데이터베이스 초기화 완료");
            } catch (Exception e) {
                log.error("테스트 데이터베이스 초기화 실패: {}", e.getMessage());
            }
        };
    }

    @Bean
    @Order(2)
    @Profile("test")
    public CommandLineRunner init() {
        return args -> {
            // 테스트용 User 생성 및 저장
            User testUser = User.builder()
                    .name("테스트유저")
                    .email("test@test.com")
                    .password(passwordEncoder.encode("1234"))
                    .phone("010-1234-5678")
                    .address("서울시 강남구 역삼동")
                    .birth(LocalDate.of(1990, 1, 1))
                    .username("testuser")
                    .build();
            userRepository.save(testUser);

            // 테스트용 AccountType 생성 및 저장
            AccountType testAccountType = AccountType.builder()
                    .code("TEST")
                    .name("테스트계좌")
                    .description("테스트용 계좌")
                    .interestRate(1.0)
                    .minimumBalance(0L)
                    .build();
            accountTypeRepository.save(testAccountType);

            List<Account> accounts = new ArrayList<>();
            for (int i = 1; i <= ACCOUNT_NUMBER; i++) {
                String accountNumber = String.format("%015d", i);
                accounts.add(Account.builder()
                        .accountNumber(accountNumber)
                        .accountName("테스트계좌" + i)
                        .accountType(testAccountType) // 저장된 AccountType 사용
                        .user(testUser) // 저장된 User 사용
                        .balance(new BigDecimal("10000"))
                        .interestRate(0.0)
                        .password(passwordEncoder.encode("1234"))
                        .build());
            }

            accountRepository.saveAll(accounts);
        };
    }

}
