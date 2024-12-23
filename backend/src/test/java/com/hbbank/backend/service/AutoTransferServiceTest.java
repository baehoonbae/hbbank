package com.hbbank.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.AutoTransfer;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.domain.enums.TransferStatus;
import com.hbbank.backend.dto.AutoTransferRequestDTO;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.exception.OutofBalanceException;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.AutoTransferRepository;

// AutoTransferService 단위 테스트
@ExtendWith(MockitoExtension.class)
class AutoTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AutoTransferRepository autoTransferRepository;
    @Mock
    private TransferService transferService;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AutoTransferService autoTransferService;

    @Test
    @DisplayName("자동이체 등록 성공")
    void register_Success() {
        // given
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        Account fromAccount = Account.builder()
                .id(1L)
                .accountNumber("0987654321")
                .password("encodedPassword")
                .user(User.builder().id(1L).build())
                .build();

        AutoTransfer expectedAutoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(fromAccount)
                .toAccountNumber(dto.getToAccountNumber())
                .amount(dto.getAmount())
                .transferDay(dto.getTransferDay())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(TransferStatus.ACTIVE)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(encoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(autoTransferRepository.save(any(AutoTransfer.class))).thenReturn(expectedAutoTransfer);

        // when
        AutoTransfer result = autoTransferService.register(dto);

        // then
        assertNotNull(result);
        assertEquals(dto.getFromAccountId(), result.getFromAccount().getId());
        assertEquals(dto.getToAccountNumber(), result.getToAccountNumber());
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(TransferStatus.ACTIVE, result.getStatus());

        verify(accountRepository).findById(1L);
        verify(encoder).matches("1234", "encodedPassword");
        verify(autoTransferRepository).save(any(AutoTransfer.class));
    }

    @Test
    @DisplayName("자동이체 등록 실패 - 계좌 없음")
    void register_Fail_AccountNotFound() {
        // given
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.register(dto));
        verify(accountRepository).findById(1L);
        verifyNoInteractions(encoder, autoTransferRepository);
    }

    @Test
    @DisplayName("자동이체 등록 실패 - 비밀번호 불일치")
    void register_Fail_WrongPassword() {
        // given
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("wrongPassword")
                .build();

        Account fromAccount = Account.builder()
                .id(1L)
                .accountNumber("0987654321")
                .password("encodedPassword")
                .user(User.builder().id(1L).build())
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(encoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.register(dto));
        verify(accountRepository).findById(1L);
        verify(encoder).matches("wrongPassword", "encodedPassword");
        verifyNoInteractions(autoTransferRepository);
    }

    @Test
    @DisplayName("자동이체 등록 실패 - 시작일이 종료일보다 늦음")
    void register_Fail_InvalidDateRange() {
        // given
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .transferDay(15)
                .startDate(LocalDate.now().plusMonths(13))
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.register(dto));
        verifyNoInteractions(accountRepository, encoder, autoTransferRepository);
    }

    @Test
    @DisplayName("자동이체 등록 실패 - 이체일이 범위를 벗어남")
    void register_Fail_InvalidTransferDay() {
        // given
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .transferDay(33) // 유효하지 않은 이체일
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.register(dto));
        verifyNoInteractions(accountRepository, encoder, autoTransferRepository);
    }

    @Test
    @DisplayName("자동이체 등록 실패 - 금액이 0 이하")
    void register_Fail_InvalidAmount() {
        // given
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("0"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.register(dto));
        verifyNoInteractions(accountRepository, encoder, autoTransferRepository);
    }

    @Test
    @DisplayName("자동이체 실행 성공")
    void executeAutoTransfer_Success() {
        // given
        LocalDate today = LocalDate.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .build();

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("10000"))
                .transferDay(today.getDayOfMonth())
                .nextTransferDate(today)
                .status(TransferStatus.ACTIVE)
                .build();

        List<AutoTransfer> autoTransfers = Collections.singletonList(autoTransfer);

        when(autoTransferRepository.findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(autoTransfers));
        when(transferService.executeTransfer(any(TransferRequestDTO.class))).thenReturn(true);

        // when
        autoTransferService.executeAutoTransfer();

        // then
        verify(autoTransferRepository).findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE);
        verify(transferService).executeTransfer(any(TransferRequestDTO.class));
        verify(autoTransferRepository).save(autoTransfer);
    }

    @Test
    @DisplayName("자동이체 실행 성공 - 다음 이체일 업데이트 검증")
    void executeAutoTransfer_Success_NextTransferDateUpdate() {
        // given
        LocalDate today = LocalDate.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .build();

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("10000"))
                .transferDay(15)
                .nextTransferDate(today)
                .status(TransferStatus.ACTIVE)
                .build();

        List<AutoTransfer> autoTransfers = Collections.singletonList(autoTransfer);

        when(autoTransferRepository.findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(autoTransfers));
        when(transferService.executeTransfer(any(TransferRequestDTO.class))).thenReturn(true);

        // when
        autoTransferService.executeAutoTransfer();

        // then
        verify(autoTransferRepository).findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE);
        verify(transferService).executeTransfer(any(TransferRequestDTO.class));
        verify(autoTransferRepository).save(autoTransfer);

        // 다음 이체일이 다음달 15일로 정확히 설정되었는지 검증
        LocalDate expectedNextTransferDate = today.plusMonths(1).withDayOfMonth(15);
        assertEquals(expectedNextTransferDate, autoTransfer.getNextTransferDate());
    }

    @Test
    @DisplayName("자동이체 실행 실패 - 이체 실패")
    void executeAutoTransfer_Fail_TransferFailed() {
        // given
        LocalDate today = LocalDate.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .build();

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("10000"))
                .transferDay(today.getDayOfMonth())
                .nextTransferDate(today)
                .status(TransferStatus.ACTIVE)
                .failureCount(0)
                .build();

        List<AutoTransfer> autoTransfers = Collections.singletonList(autoTransfer);

        when(autoTransferRepository.findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(autoTransfers));
        when(transferService.executeTransfer(any(TransferRequestDTO.class))).thenReturn(false);

        // when
        autoTransferService.executeAutoTransfer();

        // then
        verify(autoTransferRepository).findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE);
        verify(transferService).executeTransfer(any(TransferRequestDTO.class));
        verify(autoTransferRepository).save(autoTransfer);
        assertEquals(TransferStatus.ACTIVE, autoTransfer.getStatus());
        assertEquals(1, autoTransfer.getFailureCount());
    }

    @Test
    @DisplayName("자동이체 실행 실패 - 3회 실패로 상태 변경")
    void executeAutoTransfer_Fail_StatusChangeToPaused() {
        // given
        LocalDate today = LocalDate.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .build();

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("10000"))
                .transferDay(today.getDayOfMonth())
                .nextTransferDate(today)
                .status(TransferStatus.ACTIVE)
                .failureCount(2) // 이미 2회 실패한 상태
                .build();

        List<AutoTransfer> autoTransfers = Collections.singletonList(autoTransfer);

        when(autoTransferRepository.findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(autoTransfers));
        when(transferService.executeTransfer(any(TransferRequestDTO.class))).thenReturn(false);

        // when
        autoTransferService.executeAutoTransfer();

        // then
        verify(autoTransferRepository).findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE);
        verify(transferService).executeTransfer(any(TransferRequestDTO.class));
        verify(autoTransferRepository).save(autoTransfer);
        assertEquals(TransferStatus.PAUSED, autoTransfer.getStatus()); // PAUSED 상태로 변경되었는지 확인
        assertEquals(3, autoTransfer.getFailureCount()); // 실패 횟수가 3회가 되었는지 확인
    }

    @Test
    @DisplayName("자동이체 실행 실패 - 예외 발생")
    void executeAutoTransfer_Fail_Exception() {
        // given
        LocalDate today = LocalDate.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .build();

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("10000"))
                .transferDay(today.getDayOfMonth())
                .nextTransferDate(today)
                .status(TransferStatus.ACTIVE)
                .failureCount(0)
                .build();

        List<AutoTransfer> autoTransfers = Collections.singletonList(autoTransfer);

        when(autoTransferRepository.findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(autoTransfers));
        when(transferService.executeTransfer(any(TransferRequestDTO.class))).thenThrow(new RuntimeException("이체 실패"));

        // when
        autoTransferService.executeAutoTransfer();

        // then
        verify(autoTransferRepository).findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE);
        verify(transferService).executeTransfer(any(TransferRequestDTO.class));
        verify(autoTransferRepository).save(autoTransfer);
        assertEquals(1, autoTransfer.getFailureCount());
    }

    @Test
    @DisplayName("자동이체 실행 실패 - 잔액 부족")
    void executeAutoTransfer_Fail_InsufficientBalance() {
        // given
        LocalDate today = LocalDate.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .build();

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("0987654321")
                .amount(new BigDecimal("10000"))
                .transferDay(today.getDayOfMonth())
                .nextTransferDate(today)
                .status(TransferStatus.ACTIVE)
                .failureCount(0)
                .build();

        List<AutoTransfer> autoTransfers = Collections.singletonList(autoTransfer);

        when(autoTransferRepository.findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(autoTransfers));
        when(transferService.executeTransfer(any(TransferRequestDTO.class))).thenThrow(new OutofBalanceException("잔액이 부족합니다"));

        // when
        autoTransferService.executeAutoTransfer();

        // then
        verify(autoTransferRepository).findAllByNextTransferDateLessThanEqualAndStatus(today, TransferStatus.ACTIVE);
        verify(transferService).executeTransfer(any(TransferRequestDTO.class));
        verify(autoTransferRepository).save(autoTransfer);
        assertEquals(1, autoTransfer.getFailureCount());
    }

    @Test
    @DisplayName("자동이체 만료 처리 성공")
    void finishAutoTransfer_Success() {
        // given
        LocalDate today = LocalDate.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("1234567890")
                .build();

        List<AutoTransfer> autoTransfers = Arrays.asList(
                AutoTransfer.builder()
                        .id(1L)
                        .fromAccount(account)
                        .status(TransferStatus.ACTIVE)
                        .endDate(today)
                        .build(),
                AutoTransfer.builder()
                        .id(2L)
                        .fromAccount(account)
                        .status(TransferStatus.ACTIVE)
                        .endDate(today)
                        .build()
        );

        when(autoTransferRepository.findAllByEndDateAndStatus(today, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(autoTransfers));

        // when
        autoTransferService.finishAutoTransfer();

        // then
        verify(autoTransferRepository).findAllByEndDateAndStatus(today, TransferStatus.ACTIVE);
        verify(autoTransferRepository, times(2)).save(any(AutoTransfer.class));

        autoTransfers.forEach(at
                -> assertEquals(TransferStatus.COMPLETED, at.getStatus()));
    }

    @Test
    @DisplayName("자동이체 만료 처리 실패 - 예외 발생")
    void finishAutoTransfer_Fail_Exception() {
        // given
        LocalDate today = LocalDate.now();
        when(autoTransferRepository.findAllByEndDateAndStatus(today, TransferStatus.ACTIVE))
                .thenThrow(new IllegalArgumentException("리스트를 찾을 수 없습니다."));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.finishAutoTransfer());
        verify(autoTransferRepository).findAllByEndDateAndStatus(today, TransferStatus.ACTIVE);
    }

    @Test
    @DisplayName("자동이체 수정 성공")
    void update_Success() {
        // given
        Long autoTransferId = 1L;
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("20000"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        Account account = Account.builder()
                .id(1L)
                .password("encodedPassword")
                .user(User.builder().id(1L).build())
                .build();

        AutoTransfer existingAutoTransfer = AutoTransfer.builder()
                .id(autoTransferId)
                .fromAccount(account)
                .build();

        when(autoTransferRepository.findById(autoTransferId)).thenReturn(Optional.of(existingAutoTransfer));
        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.of(account));
        when(encoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(autoTransferRepository.save(any(AutoTransfer.class))).thenReturn(existingAutoTransfer);

        // when
        Optional<AutoTransfer> result = autoTransferService.update(autoTransferId, dto);

        // then
        assertTrue(result.isPresent());
        verify(autoTransferRepository).findById(autoTransferId);
        verify(accountRepository).findByIdWithUser(1L);
        verify(encoder).matches("1234", "encodedPassword");
        verify(autoTransferRepository).save(any(AutoTransfer.class));
    }

    @Test
    @DisplayName("자동이체 수정 실패 - 자동이체 없음")
    void update_Fail_AutoTransferNotFound() {
        // given
        Long autoTransferId = 1L;
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("20000"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        when(autoTransferRepository.findById(autoTransferId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.update(autoTransferId, dto));
        verify(autoTransferRepository).findById(autoTransferId);
        verifyNoInteractions(accountRepository, encoder);
    }

    @Test
    @DisplayName("자동이체 수정 실패 - 계좌 없음")
    void update_Fail_AccountNotFound() {
        // given
        Long autoTransferId = 1L;
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("20000"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("1234")
                .build();

        AutoTransfer existingAutoTransfer = AutoTransfer.builder()
                .id(autoTransferId)
                .fromAccount(Account.builder().id(1L).build())
                .build();

        when(autoTransferRepository.findById(autoTransferId)).thenReturn(Optional.of(existingAutoTransfer));
        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.update(autoTransferId, dto));
        verify(autoTransferRepository).findById(autoTransferId);
        verify(accountRepository).findByIdWithUser(1L);
        verifyNoInteractions(encoder);
    }

    @Test
    @DisplayName("자동이체 수정 실패 - 비밀번호 불일치")
    void update_Fail_WrongPassword() {
        // given
        Long autoTransferId = 1L;
        AutoTransferRequestDTO dto = AutoTransferRequestDTO.builder()
                .fromAccountId(1L)
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("20000"))
                .transferDay(15)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .password("wrongPassword")
                .build();

        Account account = Account.builder()
                .id(1L)
                .password("encodedPassword")
                .user(User.builder().id(1L).build())
                .build();

        AutoTransfer existingAutoTransfer = AutoTransfer.builder()
                .id(autoTransferId)
                .fromAccount(account)
                .build();

        when(autoTransferRepository.findById(autoTransferId)).thenReturn(Optional.of(existingAutoTransfer));
        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.of(account));
        when(encoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> autoTransferService.update(autoTransferId, dto));
        verify(autoTransferRepository).findById(autoTransferId);
        verify(accountRepository).findByIdWithUser(1L);
        verify(encoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    @DisplayName("자동이체 단건 조회 성공")
    void findById_Success() {
        // given
        Long autoTransferId = 1L;
        AutoTransfer expectedAutoTransfer = AutoTransfer.builder()
                .id(autoTransferId)
                .fromAccount(Account.builder().id(1L).build())
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .build();

        when(autoTransferRepository.findById(autoTransferId))
                .thenReturn(Optional.of(expectedAutoTransfer));

        // when
        Optional<AutoTransfer> result = autoTransferService.findById(autoTransferId);

        // then
        assertTrue(result.isPresent());
        assertEquals(autoTransferId, result.get().getId());
        verify(autoTransferRepository).findById(autoTransferId);
    }

    @Test
    @DisplayName("자동이체 단건 조회 실패 - 존재하지 않는 ID")
    void findById_NotFound() {
        // given
        Long autoTransferId = 999L;
        when(autoTransferRepository.findById(autoTransferId))
                .thenReturn(Optional.empty());

        // when
        Optional<AutoTransfer> result = autoTransferService.findById(autoTransferId);

        // then
        assertTrue(result.isEmpty());
        verify(autoTransferRepository).findById(autoTransferId);
    }

    @Test
    @DisplayName("사용자별 자동이체 목록 조회 성공")
    void findAllByUserId_Success() {
        // given
        Long userId = 1L;
        List<AutoTransfer> expectedList = Arrays.asList(
                AutoTransfer.builder()
                        .id(1L)
                        .fromAccount(Account.builder().id(1L).build())
                        .toAccountNumber("1234567890")
                        .amount(new BigDecimal("10000"))
                        .build(),
                AutoTransfer.builder()
                        .id(2L)
                        .fromAccount(Account.builder().id(1L).build())
                        .toAccountNumber("0987654321")
                        .amount(new BigDecimal("20000"))
                        .build()
        );

        when(autoTransferRepository.findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(expectedList));

        // when
        Optional<List<AutoTransfer>> result = autoTransferService.findAllByUserId(userId);

        // then
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(autoTransferRepository).findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE);
    }

    @Test
    @DisplayName("사용자별 자동이체 목록 조회 - 결과 없음")
    void findAllByUserId_Empty() {
        // given
        Long userId = 1L;
        when(autoTransferRepository.findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when
        Optional<List<AutoTransfer>> result = autoTransferService.findAllByUserId(userId);

        // then
        assertTrue(result.isEmpty());
        verify(autoTransferRepository).findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE);
    }

    @Test
    @DisplayName("자동이체 삭제 성공")
    void delete_Success() {
        // given
        AutoTransfer autoTransfer = AutoTransfer.builder()
                .id(1L)
                .fromAccount(Account.builder().id(1L).build())
                .toAccountNumber("1234567890")
                .amount(new BigDecimal("10000"))
                .build();

        // when
        autoTransferService.delete(autoTransfer);

        // then
        verify(autoTransferRepository).delete(autoTransfer);
    }
}
