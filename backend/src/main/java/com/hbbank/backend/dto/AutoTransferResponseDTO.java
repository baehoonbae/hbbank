package com.hbbank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hbbank.backend.domain.AutoTransfer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AutoTransferResponseDTO {
    private final Long id;
    private final Long fromAccountId;
    private final String toAccountNumber;
    private final BigDecimal amount;
    private final String description;
    private final int transferDay;
    private final LocalDate nextTransferDate;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public static AutoTransferResponseDTO from(AutoTransfer autoTransfer) {
        return AutoTransferResponseDTO.builder()
                .id(autoTransfer.getId())
                .fromAccountId(autoTransfer.getFromAccount().getId())
                .toAccountNumber(autoTransfer.getToAccountNumber())
                .amount(autoTransfer.getAmount())
                .description(autoTransfer.getDescription())
                .transferDay(autoTransfer.getTransferDay())
                .nextTransferDate(autoTransfer.getNextTransferDate())
                .startDate(autoTransfer.getStartDate())
                .endDate(autoTransfer.getEndDate())
                .build();
    }
    
}
