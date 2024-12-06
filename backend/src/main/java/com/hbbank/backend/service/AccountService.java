package com.hbbank.backend.service;

import org.springframework.stereotype.Service;

import com.hbbank.backend.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
}
