package com.hbbank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.AccountType;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, String> {

}
