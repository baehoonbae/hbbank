package com.hbbank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hbbank.backend.domain.ReserveTransfer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReserveTransferResponseDTO {
    private final Long id;
    private final Long fromAccountId;
    private final String toAccountNumber;
    private final BigDecimal amount;
    private final String description;
    private final LocalDateTime reservedAt;

    public static ReserveTransferResponseDTO from(ReserveTransfer rt){
        return ReserveTransferResponseDTO.builder()
                    .id(rt.getId())
                    .fromAccountId(rt.getFromAccount().getId())
                    .toAccountNumber(rt.getToAccountNumber())
                    .amount(rt.getAmount())
                    .description(rt.getDescription())
                    .reservedAt(rt.getReservedAt())
                    .build();
    }
}
