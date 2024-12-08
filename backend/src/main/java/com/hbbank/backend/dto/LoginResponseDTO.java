package com.hbbank.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private Long id;
    private String username;
    private String name;
    private String message;
} 