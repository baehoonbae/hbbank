package com.hbbank.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.hbbank.backend.service.EmailService;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(); // 테스트용 Redis 연결 팩토리
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> testRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    @Primary
    public JavaMailSender testMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    @Primary
    public EmailService testEmailService() {
        return new EmailService(testMailSender(), testRedisTemplate()) {
            @Override
            public void sendVerificationEmail(String email) {
                // 테스트에서는 실제 메일 발송하지 않음
            }
            
            @Override
            public boolean verifyEmail(String email, String code) {
                return true; // 테스트에서는 항상 인증 성공
            }
        };
    }
}
