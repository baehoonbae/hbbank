package com.hbbank.backend.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionSearchDTO {
    private final Long userId;

    private final Long accountId;
    
    @NotBlank(message = "시작일은 필수입니다")
    private final String startDate;
    
    @NotBlank(message = "종료일은 필수입니다") 
    private final String endDate;
    
    @NotNull(message = "거래 유형은 필수입니다")
    private final Integer transactionType;
    
    @NotNull(message = "페이지 번호는 필수입니다")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private final Integer page;
}
