package com.hbbank.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.exception.InvalidPasswordException;
import com.hbbank.backend.repository.AccountRepository;
import com.hbbank.backend.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    // 실제 이체 로직
    // 일단 러프하게 접근하자
    // 0. 비밀번호 검증
    // 1. 출금계좌 조회하고 돈 빼낸 다음 save
    // 2. 입금계좌 조회하고 돈 넣은 다음 save
    // 3. 거래내역 생성

    // 기본 로직은 맞지만 !! 다음 문제점들이 생긴다(동시성 관련 문제들)
    // 1. 여러 계좌에서 한 계좌에 접근하려할 때(B->A, C->A, D->A 이체)
    // 2. B->A, A->B (데드락 위험)
    // 3. 이체가 체이닝해서 일어날 때 (1->2, 2->3, 3->4, 4->5.... 999->1000)

    // 일단 1번 문제를 해결하는 방법은 각 트랜잭션(DB 접근 단위, 즉 쿼리 실행되는 부분)마다 락을 걸기(repository 레이어에서 하면된다)
    
    @Transactional
    public boolean executeTransfer(TransferRequestDTO dto) {
        
        // 출금 계좌 조회 -> 비밀번호 검증 -> 돈 빼내고 save
        Account fromAccount = accountRepository.findById(dto.getFromAccountId()).get();
        if(!passwordEncoder.matches(dto.getPassword(), fromAccount.getPassword())){
            throw new InvalidPasswordException("계좌 비밀번호가 일치하지 않습니다");
        }
        fromAccount.withdraw(dto.getAmount());
        accountRepository.save(fromAccount);

        // 입금 계좌 조회, 돈 넣고 save
        Account toAccount = accountRepository.findByAccountNumber(dto.getToAccountNumber()).get();
        toAccount.deposit(dto.getAmount());
        accountRepository.save(toAccount);

        // 거래내역 생성
        Transaction withdrawTransaction = Transaction.builder()
                .account(fromAccount)
                .transactionDateTime(LocalDateTime.now())
                .transactionType("출금")
                .sender(fromAccount.getUser().getName())
                .receiver(toAccount.getUser().getName())
                .withdrawalAmount(dto.getAmount())
                .depositAmount(BigDecimal.ZERO)
                .balance(fromAccount.getBalance())
                .build();
        Transaction depositTransaction = Transaction.builder()
                .account(toAccount)
                .transactionDateTime(LocalDateTime.now())
                .transactionType("입금")
                .sender(fromAccount.getUser().getName())
                .receiver(toAccount.getUser().getName())
                .withdrawalAmount(BigDecimal.ZERO)
                .depositAmount(dto.getAmount())
                .balance(toAccount.getBalance())
                .build();
        transactionRepository.save(withdrawTransaction);
        transactionRepository.save(depositTransaction);
        return true;
    }

}
