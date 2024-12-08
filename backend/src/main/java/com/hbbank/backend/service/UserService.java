package com.hbbank.backend.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.LoginRequestDTO;
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

    public User regist(User user) {
        User user2 = User.builder()
                .address(user.getAddress())
                .birth(user.getBirth())
                .email(user.getEmail())
                .name(user.getName())
                .password(passwordEncoder.encode(user.getPassword()))
                .phone(user.getPhone())
                .username(user.getUsername())
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
        if(passwordEncoder.matches(password, user.getPassword())){
            return opUser;
        }else{
            return Optional.empty();
        }
    }

}
