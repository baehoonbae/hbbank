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
        return userRepository.findById(userId);
    }

    public User regist(UserRegistDTO user) {
        log.info(user.getBirth().toString());
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

        return userRepository.save(user2);
    }

    public Optional<User> login(LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        Optional<User> opUser = userRepository.findByUsername(username);
        if (!opUser.isPresent() || opUser.isEmpty()) {
            return Optional.empty();
        }
        User user = opUser.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
            return opUser;
        } else {
            return Optional.empty();
        }
    }

    public void completeEmailVerification(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    userRepository.save(user);
                });
    }

    public User registOAuth2User(String email, String name) {
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

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateAdditionalInfo(Long userId, OAuth2AdditionalInfoDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        user.updateAdditionalInfo(
                dto.getBirth(),
                dto.getUsername(),
                dto.getAddress(),
                dto.getPhone()
        );

        return userRepository.save(user);
    }
}
