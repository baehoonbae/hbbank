package com.hbbank.backend.unit.service;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

import com.hbbank.backend.exception.email.InvalidVerificationCodeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

// EmailService 단위 테스트
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EmailService emailService;

    /**
     * 이메일 인증코드 발송 성공 테스트
     * 1. Redis에 인증코드 저장되는지
     * 2. 실제 이메일이 발송되는 메서드가 호출 되는지
     * 3. 모든 과정이 정상적으로 완료되는지 검증
     */
    @Test
    @DisplayName("이메일 인증코드 발송 성공")
    void sendVerificationEmail_Success() {
        // given
        String email = "test@example.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // when
        emailService.sendVerificationEmail(email);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(eq("EMAIL:" + email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    /**
     * 이메일 인증코드 발송 실패 테스트
     * 1. 메일 발송 시 예외 발생 상황 시뮬레이션
     * 2. RuntimeException이 발생하는지 검증(doThrow)
     */
    @Test
    @DisplayName("이메일 인증코드 발송 실패")
    void sendVerificationEmail_Fail() {
        // given
        String email = "test@example.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("메일 발송 실패")).when(mailSender).send(any(SimpleMailMessage.class));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendVerificationEmail(email));
        assertEquals("메일 발송 실패", exception.getMessage());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(eq("EMAIL:" + email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    /**
     * 이메일 인증 성공 테스트
     * 1. Redis에서 저장된 인증코드 조회를
     * 2. 입력된 코드와 저장된 코드 일치 확인
     * 3. 인증 성공 후 Redis에서 인증코드 삭제
     */
    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() {
        // given
        String email = "test@example.com";
        String code = "123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("EMAIL:" + email)).thenReturn(code);
        when(redisTemplate.delete("EMAIL:" + email)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> emailService.verifyEmail(email, code));
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("EMAIL:" + email);
        verify(redisTemplate).delete("EMAIL:" + email);
    }

    /**
     * 잘못된 인증코드로 인한 인증 실패 테스트
     * 1. Redis에 저장된 코드와 입력된 코드가 불일치하는 상황
     * 2. 인증 실패 시 Redis 데이터 삭제되지 않음
     */
    @Test
    @DisplayName("이메일 인증 실패 - 잘못된 코드")
    void verifyEmail_Fail_WrongCode() {
        // given
        String email = "test@example.com";
        String code = "123456";
        String wrongCode = "654321";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("EMAIL:" + email)).thenReturn(code);

        // when & then
        assertThrows(InvalidVerificationCodeException.class, () -> emailService.verifyEmail(email, wrongCode));
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("EMAIL:" + email);
        verify(redisTemplate, never()).delete(anyString());
    }

    /**
     * 만료된 인증코드로 인한 인증 실패 테스트
     * 1. Redis에서 인증코드가 만료되어 null 반환
     * 2. 인증 실패 처리 및 Redis 삭제 동작 없음 확인
     */
    @Test
    @DisplayName("이메일 인증 실패 - 만료된 코드")
    void verifyEmail_Fail_ExpiredCode() {
        // given
        String email = "test@example.com";
        String code = "123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("EMAIL:" + email)).thenReturn(null);

        // when & then
        assertThrows(InvalidVerificationCodeException.class, () -> emailService.verifyEmail(email, code));
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("EMAIL:" + email);
        verify(redisTemplate, never()).delete(anyString());
    }
}
