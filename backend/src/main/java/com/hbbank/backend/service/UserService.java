package com.hbbank.backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.repository.UserRepository;
import com.hbbank.backend.request.LoginRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public boolean regist(User user) {
        return userRepository.save(user) != null;
    }

    public Optional<User> login(LoginRequest loginRequest) {
        return userRepository.findByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword());
    }

    
}
