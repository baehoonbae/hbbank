package com.hbbank.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    
    public void sendVerificationEmail(String email) {
        // 인증 코드 생성 (6자리 숫자)
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        
        // Redis에 저장 (5분 유효)
        redisTemplate.opsForValue()
            .set("EMAIL:" + email, verificationCode, 5, TimeUnit.MINUTES);
            
        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("HB Bank 이메일 인증");
        message.setText("인증 코드: " + verificationCode + "\n"
                     + "5분 이내에 인증을 완료해주세요.");
        
        mailSender.send(message);
    }
    
    public boolean verifyEmail(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get("EMAIL:" + email);
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete("EMAIL:" + email);
            return true;
        }
        return false;
    }
}
