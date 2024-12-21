package com.hbbank.backend.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.LoginRequestDTO;
import com.hbbank.backend.dto.OAuth2AdditionalInfoDTO;
import com.hbbank.backend.dto.UserRegistDTO;
import com.hbbank.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findById(Long userId){
        log.debug("사용자 조회 시작 - 사용자ID: {}", userId);
        Optional<User> user = userRepository.findById(userId);
        log.debug("사용자 조회 완료 - 사용자ID: {}, 조회결과: {}", userId, user.isPresent());
        return user;
    }

    public User regist(UserRegistDTO user) {
        log.info("사용자 등록 시작 - 사용자명: {}, 이메일: {}", user.getUsername(), user.getEmail());
        log.debug("생년월일: {}", user.getBirth().toString());
        
        User user2 = User.builder()
                .address(user.getAddress())
                .birth(user.getBirth())
                .email(user.getEmail())
                .name(user.getName())
                .password(passwordEncoder.encode(user.getPassword()))
                .phone(user.getPhone())
                .username(user.getUsername())
                .emailVerified(true)
                .build();

        log.debug("사용자 엔티티 생성 완료");
        User savedUser = userRepository.save(user2);
        log.info("사용자 등록 완료 - 사용자ID: {}", savedUser.getId());
        return savedUser;
    }

    public Optional<User> login(LoginRequestDTO loginRequest) {
        log.info("로그인 시도 - 사용자명: {}", loginRequest.getUsername());
        
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        Optional<User> opUser = userRepository.findByUsername(username);
        log.debug("사용자 조회 결과 - 존재여부: {}", opUser.isPresent());
        
        if (!opUser.isPresent() || opUser.isEmpty()) {
            log.warn("로그인 실패 - 존재하지 않는 사용자: {}", username);
            return Optional.empty();
        }
        
        User user = opUser.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
            log.info("로그인 성공 - 사용자ID: {}", user.getId());
            return opUser;
        } else {
            log.warn("로그인 실패 - 비밀번호 불일치 (사용자명: {})", username);
            return Optional.empty();
        }
    }

    public void completeEmailVerification(String email) {
        log.info("이메일 인증 완료 처리 시작 - 이메일: {}", email);
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    log.debug("사용자 정보 업데이트 - 사용자ID: {}", user.getId());
                    userRepository.save(user);
                });
        log.info("이메일 인증 완료 처리 완료 - 이메일: {}", email);
    }

    public User registOAuth2User(String email, String name) {
        log.info("OAuth2 사용자 등록 시작 - 이메일: {}, 이름: {}", email, name);
        
        User user = User.builder()
                .email(email)
                .name(name)
                .username(email.split("@")[0])
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .isOAuth2User(true)
                .needAdditionalInfo(true)
                .birth(LocalDate.of(1900,1,1))
                .address("서울")
                .phone("010-0000-0000")
                .emailVerified(true)
                .build();

        log.debug("OAuth2 사용자 엔티티 생성 완료");
        User savedUser = userRepository.save(user);
        log.info("OAuth2 사용자 등록 완료 - 사용자ID: {}", savedUser.getId());
        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        log.debug("이메일로 사용자 조회 시작 - 이메일: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        log.debug("이메일로 사용자 조회 완료 - 이메일: {}, 조회결과: {}", email, user.isPresent());
        return user;
    }

    public User updateAdditionalInfo(Long userId, OAuth2AdditionalInfoDTO dto) {
        log.info("OAuth2 사용자 추가정보 업데이트 시작 - 사용자ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자 조회 실패 - 사용자ID: {}", userId);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다");
                });

        log.debug("추가정보 업데이트 - 사용자명: {}, 생년월일: {}", dto.getUsername(), dto.getBirth());
        user.updateAdditionalInfo(
                dto.getBirth(),
                dto.getUsername(),
                dto.getAddress(),
                dto.getPhone()
        );

        User savedUser = userRepository.save(user);
        log.info("OAuth2 사용자 추가정보 업데이트 완료 - 사용자ID: {}", userId);
        return savedUser;
    }
}
