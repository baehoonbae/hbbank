package com.hbbank.backend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private Long id;
    private String username;
    private String name;
    private String message;
} 