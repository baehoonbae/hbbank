package com.hbbank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    public Optional<List<Transaction>> findAllByAccount_IdOrderByTransactionDateTimeDesc(Long accountId);

    public Optional<List<Transaction>> findByAccountAndTransactionType(Account account, String transactionType);

}
