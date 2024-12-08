package com.hbbank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hbbank.backend.domain.AccountType;

public interface AccountTypeRepository extends JpaRepository<AccountType, String> {

}
