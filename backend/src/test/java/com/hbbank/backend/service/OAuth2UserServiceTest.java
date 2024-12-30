package com.hbbank.backend.service;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuth2User oauth2User;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    @Test
    @DisplayName("OAuth2 로그인 성공 - 기존 사용자")
    void loadUser_Success_ExistingUser() {
        // given
        String email = "test@example.com";
        String name = "테스트유저";

        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(oauth2User.getAttribute("name")).thenReturn(name);
        when(oauth2User.getAttributes()).thenReturn(Map.of("email", email, "name", name));
        when(userService.findByEmail(email)).thenReturn(
                User.builder()
                        .email(email)
                        .name(name)
                        .isOAuth2User(true)
                        .build()
        );

        // when
        OAuth2User result = oAuth2UserService.processOAuth2User(oauth2User);

        // then
        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        verify(userService).findByEmail(email);
        verify(userService, times(0)).registOAuth2User(anyString(), anyString());
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 - 신규 사용자")
    void loadUser_Success_NewUser() {
        // given
        String email = "new@example.com";
        String name = "신규유저";

        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(oauth2User.getAttribute("name")).thenReturn(name);
        when(oauth2User.getAttributes()).thenReturn(Map.of("email", email, "name", name));
        
        when(userService.findByEmail(email))
            .thenThrow(new UserNotFoundException("사용자를 찾을 수 없습니다"));
        
        when(userService.registOAuth2User(email, name))
            .thenReturn(User.builder()
                .email(email)
                .name(name)
                .build());

        // when
        OAuth2User result = oAuth2UserService.processOAuth2User(oauth2User);

        // then
        assertEquals(email, result.getAttribute("email"));
        assertEquals(name, result.getAttribute("name"));
        
        verify(userService).findByEmail(email);
        verify(userService).registOAuth2User(email, name);
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 이메일 누락")
    void loadUser_Failure_MissingEmail() {
        // given
        when(oauth2User.getAttribute("email")).thenReturn(null);
        when(oauth2User.getAttributes()).thenReturn(Map.of());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            oAuth2UserService.processOAuth2User(oauth2User);
        });
        verify(userService, never()).findByEmail(anyString());
        verify(userService, never()).registOAuth2User(anyString(), anyString());
    }
}
