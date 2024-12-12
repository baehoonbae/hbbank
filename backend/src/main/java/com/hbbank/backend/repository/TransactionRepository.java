package com.hbbank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.dto.TransactionSearchDTO;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    public Optional<List<Transaction>> findAllByAccount_IdOrderByTransactionDateTimeDesc(Long accountId);

    public Optional<List<Transaction>> findByAccountAndTransactionType(Account account, String transactionType);

    @Query("SELECT t FROM Transaction t " +
           "WHERE (:#{#dto.accountId} = 0 OR :#{#dto.accountId} is null OR :#{#dto.accountId} = t.account.id) " +
           "AND CAST(t.transactionDateTime AS date) BETWEEN CAST(:#{#dto.startDate} AS date) AND CAST(:#{#dto.endDate} AS date) " +
           "AND (:#{#dto.transactionType} = 0 OR " +
           "    (:#{#dto.transactionType} = 1 AND t.transactionType = '입금') OR " + 
           "    (:#{#dto.transactionType} = 2 AND t.transactionType = '출금')) " +
           "ORDER BY t.transactionDateTime DESC")
    public Optional<List<Transaction>> findAllByCondition(TransactionSearchDTO dto);
}
