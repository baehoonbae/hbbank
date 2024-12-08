package com.hbbank.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
}
