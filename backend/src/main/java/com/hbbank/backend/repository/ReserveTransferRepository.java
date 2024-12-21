package com.hbbank.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.ReserveTransfer;
import com.hbbank.backend.domain.enums.TransferStatus;

@Repository
public interface ReserveTransferRepository extends JpaRepository<ReserveTransfer, Long> {

    public Optional<List<ReserveTransfer>> findAllByUserIdAndStatus(Long userId, TransferStatus status);

    @Query("select rt from ReserveTransfer rt where rt.reservedAt <= :reservedAt and rt.status='ACTIVE'")
    public Optional<List<ReserveTransfer>> findAllPendingTransfers(@Param("reservedAt") LocalDateTime now);
}
