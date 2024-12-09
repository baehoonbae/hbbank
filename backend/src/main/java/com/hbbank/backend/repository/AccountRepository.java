package com.hbbank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    public Optional<List<Account>> findAllByUser_Id(Long userId);

    public Optional<Account> findByAccountNumber(String accountNumber);
}
