package com.hbbank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbbank.backend.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
