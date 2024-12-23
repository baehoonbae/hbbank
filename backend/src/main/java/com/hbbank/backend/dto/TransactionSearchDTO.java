package com.hbbank.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionSearchDTO {
    @NotNull(message = "사용자 ID는 필수입니다")
    private final Long userId;

    @NotNull(message = "계좌 ID는 필수입니다")
    private final Long accountId;
    
    @NotBlank(message = "시작일은 필수입니다")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$", 
            message = "시작일은 yyyy-MM-dd 형식이어야 합니다")
    private final String startDate;
    
    @NotBlank(message = "종료일은 필수입니다")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$", 
            message = "종료일은 yyyy-MM-dd 형식이어야 합니다")
    private final String endDate;
    
    @NotNull(message = "거래 유형은 필수입니다")
    @Min(value = 0, message = "거래 유형은 0 이상이어야 합니다")
    @Max(value = 2, message = "거래 유형은 2 이하여야 합니다")
    private final Integer transactionType;
    
    @NotNull(message = "페이지 번호는 필수입니다")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private final Integer page;
}
