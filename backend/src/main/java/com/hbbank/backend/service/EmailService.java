package com.hbbank.backend.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.hbbank.backend.exception.email.InvalidVerificationCodeException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendVerificationEmail(String email) {
        log.info("이메일 인증 코드 발송 시작 - 이메일: {}", email);

        // 인증 코드 생성 (6자리 숫자)
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        log.debug("인증 코드 생성 완료: {}", verificationCode);

        // Redis에 저장 (5분 유효)
        redisTemplate.opsForValue().set("EMAIL:" + email, verificationCode, 5, TimeUnit.MINUTES);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("HB Bank 이메일 인증");
        message.setText("인증 코드: " + verificationCode + "\n"
                + "5분 이내에 인증을 완료해주세요.");

        try {
            mailSender.send(message);
            log.info("이메일 인증 코드 발송 완료 - 이메일: {}", email);
        } catch (MailException e) {
            log.error("이메일 발송 실패 - 이메일: {}, 사유: {}", email, e.getMessage());
            throw e;
        }
    }

    public void verifyEmail(String email, String code) {
        log.info("이메일 인증 코드 검증 시작 - 이메일: {}", email);
        String storedCode = redisTemplate.opsForValue().get("EMAIL:" + email);

        if (storedCode == null || !storedCode.equals(code)) {
            log.warn("이메일 인증 실패 - 이메일: {}", email);
            throw new InvalidVerificationCodeException("인증 코드가 일치하지 않습니다.");
        }

        redisTemplate.delete("EMAIL:" + email);
        log.info("이메일 인증 성공 - 이메일: {}", email);
    }
}
