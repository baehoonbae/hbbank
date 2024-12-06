package com.hbbank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbbank.backend.domain.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
