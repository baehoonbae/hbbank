package com.hbbank.backend.dto;

public class AdditionalInfoResponseDTO extends RuntimeException {

    public AdditionalInfoResponseDTO() {
        super("추가 정보가 성공적으로 저장되었습니다.");
    }

    public AdditionalInfoResponseDTO(String msg) {
        super(msg);
    }
}
