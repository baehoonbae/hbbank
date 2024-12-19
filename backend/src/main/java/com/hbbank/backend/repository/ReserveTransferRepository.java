package com.hbbank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hbbank.backend.domain.ReserveTransfer;

@Repository
public interface ReserveTransferRepository extends JpaRepository<ReserveTransfer, Long> {

}
