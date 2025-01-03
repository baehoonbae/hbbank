package com.hbbank.backend.dto;

import com.hbbank.backend.domain.Transaction;
import com.hbbank.backend.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private Long id;
    private String username;
    private String name;
    private String message;

    public static LoginResponseDTO from(String accessToken, User user) {
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .message("로그인 성공!")
                .build();
    }

} 