package com.hbbank.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.AutoTransfer;
import com.hbbank.backend.domain.enums.AutoTransferStatus;

@Repository
public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {

    public Optional<List<AutoTransfer>> findAllByUserIdAndStatus(Long userId, AutoTransferStatus status);

    public Optional<List<AutoTransfer>> findAllByNextTransferDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AutoTransferStatus status);

    public Optional<List<AutoTransfer>> findAllByEndDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AutoTransferStatus status);

}
