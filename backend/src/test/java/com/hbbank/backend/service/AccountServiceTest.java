package com.hbbank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.hbbank.backend.exception.account.*;
import com.hbbank.backend.exception.user.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AccountType;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.domain.enums.AccountStatus;
import com.hbbank.backend.dto.AccountCreateDTO;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.AccountTypeRepository;
import com.hbbank.backend.util.AccountNumberGenerator;

// AccountService 단위 테스트
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountTypeRepository accountTypeRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private AccountNumberGenerator numGen;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccount_Success() {
        // given
        AccountCreateDTO dto = AccountCreateDTO.builder()
                .userId(1L)
                .accountTypeCode("001")
                .balance(new BigDecimal("10000"))
                .password("1234")
                .build();

        User user = User.builder()
                .id(1L)
                .name("홍길동")
                .build();

        AccountType accountType = AccountType.builder()
                .code("001")
                .name("입출금통장")
                .description("일반 입출금 통장")
                .interestRate(0.1)
                .minimumBalance(0L)
                .defaultTransferLimit(new BigDecimal("5000000"))
                .defaultDailyTransferLimit(new BigDecimal("10000000"))
                .build();

        String accountNumber = "1234567890";
        String encodedPassword = "encodedPassword";

        Account expectedAccount = Account.builder()
                .user(user)
                .accountType(accountType)
                .accountName(accountType.getName())
                .accountNumber(accountNumber)
                .balance(new BigDecimal("10000"))
                .interestRate(accountType.getInterestRate())
                .password(encodedPassword)
                .build();

        when(userService.findById(1L)).thenReturn(user);
        when(accountTypeRepository.findById("001")).thenReturn(Optional.of(accountType));
        when(numGen.generate("001")).thenReturn(accountNumber);
        when(encoder.encode("1234")).thenReturn(encodedPassword);
        when(accountRepository.save(any(Account.class))).thenReturn(expectedAccount);

        // when
        Account result = accountService.createAccount(dto);

        // then
        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
        assertEquals(user, result.getUser());
        assertEquals(accountType, result.getAccountType());
        assertEquals(new BigDecimal("10000"), result.getBalance());
        assertEquals(encodedPassword, result.getPassword());

        verify(userService).findById(1L);
        verify(accountTypeRepository).findById("001");
        verify(numGen).generate("001");
        verify(encoder).encode("1234");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자 없음")
    void createAccount_UserNotFound() {
        // given
        AccountCreateDTO dto = AccountCreateDTO.builder()
                .userId(33333L)
                .accountTypeCode("001")
                .password("1234")
                .balance(new BigDecimal("10000"))
                .build();
        when(userService.findById(dto.getUserId())).thenThrow(UserNotFoundException.class);

        // when & then
        assertThrows(UserNotFoundException.class, () -> accountService.createAccount(dto));
        verify(userService).findById(any());
    }

    @Test
    @DisplayName("계좌 생성 실패 - 계좌유형 없음")
    void createAccount_AccountTypeNotFound() {
        // given
        AccountCreateDTO dto = AccountCreateDTO.builder()
                .userId(1L)
                .accountTypeCode("999")
                .password("1234")
                .balance(new BigDecimal("10000"))
                .build();

        User user = User.builder()
                .id(1L)
                .name("홍길동")
                .build();

        when(userService.findById(1L)).thenReturn(user);
        when(accountTypeRepository.findById("999")).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> accountService.createAccount(dto));
        verify(userService).findById(1L);
        verify(accountTypeRepository).findById("999");
        verifyNoInteractions(numGen, encoder, accountRepository);
    }

    @Test
    @DisplayName("계좌 유형 목록 조회 성공")
    void getAccountTypes_Success() {
        // given
        List<AccountType> expectedTypes = Arrays.asList(
                AccountType.builder().code("001").name("입출금통장").build(),
                AccountType.builder().code("002").name("적금통장").build()
        );
        when(accountTypeRepository.findAll()).thenReturn(expectedTypes);

        // when
        List<AccountType> result = accountService.getAccountTypes();

        // then
        assertEquals(2, result.size());
        verify(accountTypeRepository).findAll();
    }

    @Test
    @DisplayName("계좌번호로 계좌 조회 성공")
    void findByAccountNumber_Success() {
        // given
        String accountNumber = "1234567890";
        Account expectedAccount = Account.builder()
                .accountNumber(accountNumber)
                .build();
        when(accountRepository.findByAccountNumberWithUser(accountNumber))
                .thenReturn(Optional.of(expectedAccount));

        // when
        Account result = accountService.findByAccountNumber(accountNumber);

        // then
        assertEquals(accountNumber, result.getAccountNumber());
        verify(accountRepository).findByAccountNumberWithUser(accountNumber);
    }

    @Test
    @DisplayName("사용자 ID로 계좌 목록 조회 성공")
    void findAllByUser_Id_Success() {
        // given
        Long userId = 1L;
        List<Account> expectedAccounts = Arrays.asList(
                Account.builder().accountNumber("1111").build(),
                Account.builder().accountNumber("2222").build()
        );
        when(accountRepository.findAllByUser_IdWithUser(userId))
                .thenReturn(Optional.of(expectedAccounts));

        // when
        List<Account> result = accountService.findAllByUser_Id(userId);

        // then
        assertEquals(2, result.size());
        verify(accountRepository).findAllByUser_IdWithUser(userId);
    }

    @Test
    @DisplayName("계좌 ID로 계좌 조회 성공")
    void findById_Success() {
        // given
        Long accountId = 1L;
        Account expectedAccount = Account.builder()
                .id(accountId)
                .accountNumber("1234567890")
                .build();
        when(accountRepository.findByIdWithUser(accountId))
                .thenReturn(Optional.of(expectedAccount));

        // when
        Account result = accountService.findById(accountId);

        // then
        assertEquals(accountId, result.getId());
        verify(accountRepository).findByIdWithUser(accountId);
    }

    @Test
    @DisplayName("계좌 ID로 계좌 조회 실패 - 계좌 없음")
    void findById_NotFound() {
        // given
        Long accountId = 1L;
        when(accountRepository.findByIdWithUser(accountId))
                .thenReturn(Optional.empty());

        // when&then
        assertThrows(AccountNotFoundException.class, () -> accountService.findById(accountId));
        verify(accountRepository).findByIdWithUser(accountId);
    }

    @Test
    @DisplayName("일일 이체 한도 초기화 성공")
    void resetDailyTransferAmount_Success() {
        // when
        accountService.resetDailyTransferAmount();

        // then
        verify(accountRepository).resetAllDailyTransferredAmounts();
    }

    @Test
    @DisplayName("출금 실패 - 계좌 상태 비활성화")
    void withdraw_InvalidAccountStatus() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.DORMANT)
                .build();

        // when & then
        assertThrows(InvalidAccountStatusException.class,
                () -> account.withdraw(new BigDecimal("10000")));
    }

    @Test
    @DisplayName("출금 실패 - 1회 이체한도 초과")
    void withdraw_TransferLimitExceeded() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("1000000"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        // when & then
        assertThrows(TransferLimitExceededException.class,
                () -> account.withdraw(new BigDecimal("600000")));
    }

    @Test
    @DisplayName("출금 실패 - 일일 이체한도 초과")
    void withdraw_DailyTransferLimitExceeded() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("1000000"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(new BigDecimal("800000"))
                .build();

        // when & then
        assertThrows(DailyTransferLimitExceededException.class,
                () -> account.withdraw(new BigDecimal("300000")));
    }

    @Test
    @DisplayName("출금 실패 - 잔액 부족")
    void withdraw_OutOfBalance() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("100000"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        // when & then
        assertThrows(OutofBalanceException.class,
                () -> account.withdraw(new BigDecimal("200000")));
    }

    @Test
    @DisplayName("출금 성공")
    void withdraw_Success() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("100000"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        // when
        account.withdraw(new BigDecimal("50000"));

        // then
        assertEquals(new BigDecimal("50000"), account.getBalance());
        assertEquals(new BigDecimal("50000"), account.getDailyTransferredAmount());
    }

    @Test
    @DisplayName("입금 성공")
    void deposit_Success() {
        // given
        Account account = Account.builder()
                .balance(new BigDecimal("100000"))
                .status(AccountStatus.ACTIVE)
                .build();

        // when
        account.deposit(new BigDecimal("50000"));

        // then
        assertEquals(new BigDecimal("150000"), account.getBalance());
    }

    @Test
    @DisplayName("입금 실패 - 유효하지 않은 계좌 상태")
    void deposit_Fail_InvalidAccountStatus() {
        // given
        Account account = Account.builder()
                .balance(new BigDecimal("100000"))
                .status(AccountStatus.DORMANT)
                .build();

        // when & then
        assertThrows(InvalidAccountStatusException.class, () -> account.deposit(new BigDecimal("50000")));
    }

    @Test
    @DisplayName("계좌 상태 변경 테스트 - 휴면")
    void accountStatus_Dormant() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.DORMANT)
                .balance(new BigDecimal("100000"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        // when & then
        assertThrows(InvalidAccountStatusException.class, () -> {
            account.withdraw(new BigDecimal("10000"));
        });
    }

    @Test
    @DisplayName("계좌 상태 변경 테스트 - 정지")
    void accountStatus_Blocked() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.BLOCKED)
                .balance(new BigDecimal("100000"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        // when & then
        assertThrows(InvalidAccountStatusException.class, () -> {
            account.withdraw(new BigDecimal("10000"));
        });
    }

    @Test
    @DisplayName("계좌 상태 변경 테스트 - 해지")
    void accountStatus_Closed() {
        // given
        Account account = Account.builder()
                .status(AccountStatus.CLOSED)
                .balance(new BigDecimal("100000"))
                .transferLimit(new BigDecimal("500000"))
                .dailyTransferLimit(new BigDecimal("1000000"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .build();

        // when & then
        assertThrows(InvalidAccountStatusException.class, () -> {
            account.withdraw(new BigDecimal("10000"));
        });
    }
}