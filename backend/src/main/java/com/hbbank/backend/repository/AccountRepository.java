package com.hbbank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.Account;

import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    public Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("select a from Account a join fetch a.user where a.id=:id")
    public Optional<Account> findByIdWithUser(@Param("id") Long id);

    @Query("select a from Account a join fetch a.user where a.user.id=:userId")
    public Optional<List<Account>> findAllByUser_IdWithUser(@Param("userId") Long userId);

    @Query("select a from Account a join fetch a.user where a.accountNumber=:accountNumber")
    public Optional<Account> findByAccountNumberWithUser(@Param("accountNumber") String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("select a from Account a join fetch a.user where a.id=:id")
    public Optional<Account> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("select a from Account a join fetch a.user where a.accountNumber=:accountNumber")
    public Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

    @Query("update Account a set a.dailyTransferredAmount = 0")
    @Modifying
    public void resetAllDailyTransferredAmounts();
}
