package com.hbbank.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.hbbank.backend.exception.account.AccountNotFoundException;
import com.hbbank.backend.exception.reserveTransfer.InvalidReserveTransferPasswordException;
import com.hbbank.backend.exception.reserveTransfer.ReserveTransferNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.ReserveTransfer;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.domain.enums.TransferStatus;
import com.hbbank.backend.dto.ReserveTransferRequestDTO;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.ReserveTransferRepository;

// ReserveTransferService 단위 테스트
@ExtendWith(MockitoExtension.class)
class ReserveTransferServiceTest {

    @Mock
    private ReserveTransferRepository reserveTransferRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferService transferService;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private ReserveTransferService reserveTransferService;

    @Test
    @DisplayName("예약이체 등록 성공")
    void register_Success() {
        // given
        ReserveTransferRequestDTO dto = ReserveTransferRequestDTO.builder()
                .userId(1L)
                .fromAccountId(1L)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("테스트 이체")
                .reservedAt(LocalDateTime.now().plusDays(1))
                .password("1234")
                .build();

        Account fromAccount = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .password("encodedPassword")
                .balance(new BigDecimal("50000"))
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer expectedTransfer = ReserveTransfer.builder()
                .id(1L)
                .fromAccount(fromAccount)
                .toAccountNumber(dto.getToAccountNumber())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .status(TransferStatus.ACTIVE)
                .reservedAt(dto.getReservedAt())
                .build();

        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.of(fromAccount));
        when(encoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(reserveTransferRepository.save(any(ReserveTransfer.class))).thenReturn(expectedTransfer);

        // when
        ReserveTransfer result = reserveTransferService.register(dto);

        // then
        assertNotNull(result);
        assertEquals(dto.getFromAccountId(), result.getFromAccount().getId());
        assertEquals(dto.getToAccountNumber(), result.getToAccountNumber());
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(TransferStatus.ACTIVE, result.getStatus());
        assertEquals(dto.getReservedAt(), result.getReservedAt());

        verify(accountRepository).findByIdWithUser(1L);
        verify(encoder).matches("1234", "encodedPassword");
        verify(reserveTransferRepository).save(any(ReserveTransfer.class));
    }

    @Test
    @DisplayName("예약이체 등록 실패 - 계좌 없음")
    void register_Fail_AccountNotFound() {
        // given
        ReserveTransferRequestDTO dto = ReserveTransferRequestDTO.builder()
                .userId(1L)
                .fromAccountId(1L)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("테스트 이체")
                .reservedAt(LocalDateTime.now().plusDays(1))
                .password("1234")
                .build();

        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(AccountNotFoundException.class, () -> reserveTransferService.register(dto));
        verify(accountRepository).findByIdWithUser(1L);
        verifyNoInteractions(encoder, reserveTransferRepository);
    }

    @Test
    @DisplayName("예약이체 등록 실패 - 비밀번호 불일치")
    void register_Fail_WrongPassword() {
        // given
        ReserveTransferRequestDTO dto = ReserveTransferRequestDTO.builder()
                .userId(1L)
                .fromAccountId(1L)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("테스트 이체")
                .reservedAt(LocalDateTime.now().plusDays(1))
                .password("wrongPassword")
                .build();

        Account fromAccount = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .password("encodedPassword")
                .user(User.builder().id(1L).build())
                .build();

        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.of(fromAccount));
        when(encoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThrows(InvalidReserveTransferPasswordException.class, () -> reserveTransferService.register(dto));
        verify(accountRepository).findByIdWithUser(1L);
        verify(encoder).matches("wrongPassword", "encodedPassword");
        verifyNoInteractions(reserveTransferRepository);
    }

    @Test
    @DisplayName("예약이체 등록 실패 - 과거 시간 예약")
    void register_Fail_PastDateTime() {
        // given
        ReserveTransferRequestDTO dto = ReserveTransferRequestDTO.builder()
                .userId(1L)
                .fromAccountId(1L)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("테스트 이체")
                .reservedAt(LocalDateTime.now().minusDays(1))
                .password("1234")
                .build();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> reserveTransferService.register(dto));
        verifyNoInteractions(accountRepository, encoder, reserveTransferRepository);
    }

    @Test
    @DisplayName("예약이체 수정 성공")
    void update_Success() {
        // given
        Long id = 1L;
        ReserveTransferRequestDTO dto = ReserveTransferRequestDTO.builder()
                .userId(1L)
                .fromAccountId(1L)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("20000"))
                .description("수정된 이체")
                .reservedAt(LocalDateTime.now().plusDays(2))
                .password("1234")
                .build();

        Account fromAccount = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .password("encodedPassword")
                .balance(new BigDecimal("50000"))
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer existingTransfer = ReserveTransfer.builder()
                .id(id)
                .fromAccount(fromAccount)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("원래 이체")
                .status(TransferStatus.ACTIVE)
                .reservedAt(LocalDateTime.now().plusDays(1))
                .build();

        when(reserveTransferRepository.findById(id)).thenReturn(Optional.of(existingTransfer));
        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.of(fromAccount));
        when(encoder.matches(dto.getPassword(), fromAccount.getPassword())).thenReturn(true);
        when(reserveTransferRepository.save(any(ReserveTransfer.class))).thenReturn(existingTransfer);

        // when
        ReserveTransfer result = reserveTransferService.update(id, dto);

        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(dto.getAmount(), result.getAmount()),
                () -> assertEquals(dto.getDescription(), result.getDescription()),
                () -> assertEquals(dto.getReservedAt(), result.getReservedAt())
        );
        verify(reserveTransferRepository).findById(id);
        verify(accountRepository).findByIdWithUser(1L);
        verify(encoder).matches(dto.getPassword(), fromAccount.getPassword());
        verify(reserveTransferRepository).save(any(ReserveTransfer.class));
    }

    @Test
    @DisplayName("예약이체 수정 실패 - 예약이체 없음")
    void update_Fail_TransferNotFound() {
        // given
        Long id = 1L;
        ReserveTransferRequestDTO dto = ReserveTransferRequestDTO.builder()
                .userId(1L)
                .fromAccountId(1L)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("20000"))
                .description("수정된 이체")
                .reservedAt(LocalDateTime.now().plusDays(2))
                .password("1234")
                .build();

        when(reserveTransferRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ReserveTransferNotFoundException.class, () -> reserveTransferService.update(id, dto));
        verify(reserveTransferRepository).findById(id);
        verifyNoInteractions(accountRepository, encoder);
    }

    @Test
    @DisplayName("예약이체 수정 실패 - 비밀번호 불일치")
    void update_Fail_WrongPassword() {
        // given
        Long id = 1L;
        ReserveTransferRequestDTO dto = ReserveTransferRequestDTO.builder()
                .userId(1L)
                .fromAccountId(1L)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("20000"))
                .description("수정된 이체")
                .reservedAt(LocalDateTime.now().plusDays(2))
                .password("wrongPassword")
                .build();

        Account fromAccount = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .password("encodedPassword")
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer existingTransfer = ReserveTransfer.builder()
                .id(id)
                .fromAccount(fromAccount)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .status(TransferStatus.ACTIVE)
                .build();

        when(reserveTransferRepository.findById(id)).thenReturn(Optional.of(existingTransfer));
        when(accountRepository.findByIdWithUser(1L)).thenReturn(Optional.of(fromAccount));
        when(encoder.matches(dto.getPassword(), fromAccount.getPassword())).thenReturn(false);

        // when & then
        assertThrows(InvalidReserveTransferPasswordException.class, () -> reserveTransferService.update(id, dto));
        verify(reserveTransferRepository).findById(id);
        verify(accountRepository).findByIdWithUser(1L);
        verify(encoder).matches(dto.getPassword(), fromAccount.getPassword());
    }

    @Test
    @DisplayName("예약이체 조회 성공")
    void findById_Success() {
        // given
        Long id = 1L;
        Account account = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .balance(new BigDecimal("50000"))
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer transfer = ReserveTransfer.builder()
                .id(id)
                .fromAccount(account)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("테스트 이체")
                .status(TransferStatus.ACTIVE)
                .reservedAt(LocalDateTime.now().plusDays(1))
                .build();

        when(reserveTransferRepository.findById(id)).thenReturn(Optional.of(transfer));

        // when
        ReserveTransfer result = reserveTransferService.findById(id);

        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(id, result.getId()),
                () -> assertEquals(account.getId(), result.getFromAccount().getId()),
                () -> assertEquals("123456789012345", result.getToAccountNumber()),
                () -> assertEquals(new BigDecimal("10000"), result.getAmount()),
                () -> assertEquals("테스트 이체", result.getDescription()),
                () -> assertEquals(TransferStatus.ACTIVE, result.getStatus())
        );


        verify(reserveTransferRepository).findById(id);
    }

    @Test
    @DisplayName("사용자별 예약이체 목록 조회 성공")
    void findAllByUserId_Success() {
        // given
        Long userId = 1L;
        Account account = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .balance(new BigDecimal("50000"))
                .user(User.builder().id(userId).build())
                .build();

        List<ReserveTransfer> transfers = Arrays.asList(
                ReserveTransfer.builder()
                        .id(1L)
                        .fromAccount(account)
                        .toAccountNumber("123456789012345")
                        .amount(new BigDecimal("10000"))
                        .description("첫번째 이체")
                        .status(TransferStatus.ACTIVE)
                        .reservedAt(LocalDateTime.now().plusDays(1))
                        .build(),
                ReserveTransfer.builder()
                        .id(2L)
                        .fromAccount(account)
                        .toAccountNumber("987654321012345")
                        .amount(new BigDecimal("20000"))
                        .description("두번째 이체")
                        .status(TransferStatus.ACTIVE)
                        .reservedAt(LocalDateTime.now().plusDays(2))
                        .build()
        );

        when(reserveTransferRepository.findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE))
                .thenReturn(Optional.of(transfers));

        // when
        List<ReserveTransfer> result = reserveTransferService.findAllByUserId(userId);

        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("첫번째 이체", result.get(0).getDescription()),
                () -> assertEquals("두번째 이체", result.get(1).getDescription()),
                () -> assertEquals(new BigDecimal("10000"), result.get(0).getAmount()),
                () -> assertEquals(new BigDecimal("20000"), result.get(1).getAmount())
        );


        verify(reserveTransferRepository).findAllByUserIdAndStatus(userId, TransferStatus.ACTIVE);
    }

    @Test
    @DisplayName("예약이체 실행 성공")
    void executeReserveTransfers_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .balance(new BigDecimal("50000"))
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer transfer1 = ReserveTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("첫번째 이체")
                .status(TransferStatus.ACTIVE)
                .reservedAt(now.minusMinutes(1))
                .build();

        ReserveTransfer transfer2 = ReserveTransfer.builder()
                .id(2L)
                .fromAccount(account)
                .toAccountNumber("987654321012345")
                .amount(new BigDecimal("20000"))
                .description("두번째 이체")
                .status(TransferStatus.ACTIVE)
                .reservedAt(now.minusMinutes(2))
                .build();

        when(reserveTransferRepository.findAllPendingTransfers(any(LocalDateTime.class)))
                .thenReturn(Optional.of(Arrays.asList(transfer1, transfer2)));
        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(true);

        // when
        reserveTransferService.executeReserveTransfers();

        // then
        verify(reserveTransferRepository).findAllPendingTransfers(any(LocalDateTime.class));
        verify(transferService, times(2)).transfer(any(TransferRequestDTO.class));
        verify(reserveTransferRepository, times(2)).save(any(ReserveTransfer.class));
        assertEquals(TransferStatus.COMPLETED, transfer1.getStatus());
        assertEquals(TransferStatus.COMPLETED, transfer2.getStatus());
    }

    @Test
    @DisplayName("예약이체 실행 실패 - 3회 실패 시 일시정지")
    void executeReserveTransfers_FailThreeTimes() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .balance(new BigDecimal("50000"))
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer transfer = ReserveTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("실패하는 이체")
                .status(TransferStatus.ACTIVE)
                .reservedAt(now.minusMinutes(1))
                .failureCount(2)
                .build();

        when(reserveTransferRepository.findAllPendingTransfers(any(LocalDateTime.class)))
                .thenReturn(Optional.of(List.of(transfer)));
        when(transferService.transfer(any(TransferRequestDTO.class))).thenReturn(false);

        // when
        reserveTransferService.executeReserveTransfers();

        // then
        verify(reserveTransferRepository).findAllPendingTransfers(any(LocalDateTime.class));
        verify(transferService).transfer(any(TransferRequestDTO.class));
        verify(reserveTransferRepository).save(transfer);
        assertEquals(TransferStatus.PAUSED, transfer.getStatus());
        assertEquals(3, transfer.getFailureCount());
    }

    @Test
    @DisplayName("예약이체 실행 실패 - 예외 발생")
    void executeReserveTransfers_Exception() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .balance(new BigDecimal("50000"))
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer transfer = ReserveTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("예외 발생 이체")
                .status(TransferStatus.ACTIVE)
                .reservedAt(now.minusMinutes(1))
                .failureCount(0)
                .build();

        when(reserveTransferRepository.findAllPendingTransfers(any(LocalDateTime.class)))
                .thenReturn(Optional.of(List.of(transfer)));
        when(transferService.transfer(any(TransferRequestDTO.class)))
                .thenThrow(new RuntimeException("이체 실행 중 오류 발생"));

        // when
        reserveTransferService.executeReserveTransfers();

        // then
        verify(reserveTransferRepository).findAllPendingTransfers(any(LocalDateTime.class));
        verify(transferService).transfer(any(TransferRequestDTO.class));
        verify(reserveTransferRepository).save(transfer);
        assertEquals(1, transfer.getFailureCount());
        assertEquals(TransferStatus.ACTIVE, transfer.getStatus());
    }

    @Test
    @DisplayName("예약이체 실행 실패 - 잔액 부족")
    void executeReserveTransfers_InsufficientBalance() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("987654321012345")
                .balance(new BigDecimal("5000"))
                .user(User.builder().id(1L).build())
                .build();

        ReserveTransfer transfer = ReserveTransfer.builder()
                .id(1L)
                .fromAccount(account)
                .toAccountNumber("123456789012345")
                .amount(new BigDecimal("10000"))
                .description("잔액 부족 이체")
                .status(TransferStatus.ACTIVE)
                .reservedAt(now.minusMinutes(1))
                .failureCount(0)
                .build();

        when(reserveTransferRepository.findAllPendingTransfers(any(LocalDateTime.class)))
                .thenReturn(Optional.of(List.of(transfer)));
        when(transferService.transfer(any(TransferRequestDTO.class)))
                .thenReturn(false);

        // when
        reserveTransferService.executeReserveTransfers();

        // then
        verify(reserveTransferRepository).findAllPendingTransfers(any(LocalDateTime.class));
        verify(transferService).transfer(any(TransferRequestDTO.class));
        verify(reserveTransferRepository).save(transfer);
        assertEquals(TransferStatus.ACTIVE, transfer.getStatus());
    }
}
