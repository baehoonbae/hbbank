package com.hbbank.backend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hbbank.backend.domain.RefreshToken;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.TokenResponseDTO;
import com.hbbank.backend.exception.token.InvalidTokenException;
import com.hbbank.backend.repository.RefreshTokenRepository;
import com.hbbank.backend.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    // 토큰 생성 테스트
    @Test
    @DisplayName("토큰 생성 성공 - 신규 사용자")
    void createTokens_Success_NewUser() {
        // given
        Long userId = 1L;
        String accessToken = "test.access.token";
        String refreshToken = "test.refresh.token";

        when(jwtUtil.createAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(userId)).thenReturn(refreshToken);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(null);

        // when
        TokenResponseDTO result = tokenService.createTokens(userId);

        // then
        assertNotNull(result);
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰 생성 성공 - 기존 사용자 토큰 갱신")
    void createTokens_Success_ExistingUser() {
        // given
        Long userId = 1L;
        String accessToken = "test.access.token";
        String refreshToken = "test.refresh.token";

        RefreshToken existingToken = RefreshToken.builder()
                .user(User.builder().id(userId).build())
                .token("old.refresh.token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtUtil.createAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(userId)).thenReturn(refreshToken);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(existingToken);

        // when
        TokenResponseDTO result = tokenService.createTokens(userId);

        // then
        assertNotNull(result);
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
        verify(refreshTokenRepository).updateRefreshToken(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰 생성 실패 - JwtUtil 예외 발생")
    void createTokens_Failure_JwtException() {
        // given
        Long userId = 1L;
        when(jwtUtil.createAccessToken(userId)).thenThrow(new RuntimeException("토큰 생성 실패"));

        // when & then
        assertThrows(RuntimeException.class, ()
                -> tokenService.createTokens(userId)
        );
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("토큰 생성 실패 - 리포지토리 예외")
    void createTokens_Failure_RepositoryException() {
        // given
        Long userId = 1L;
        String accessToken = "test.access.token";
        String refreshToken = "test.refresh.token";

        when(jwtUtil.createAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(userId)).thenReturn(refreshToken);
        when(refreshTokenRepository.findByUser_Id(userId))
                .thenThrow(new RuntimeException("DB 연결 오류"));

        // when & then
        assertThrows(RuntimeException.class, ()
                -> tokenService.createTokens(userId)
        );
        verify(jwtUtil).createAccessToken(userId);
        verify(jwtUtil).createRefreshToken(userId);
        verify(refreshTokenRepository).findByUser_Id(userId);
    }

    @Test
    @DisplayName("리프레시 토큰 저장 실패")
    void createTokens_Failure_SaveError() {
        // given
        Long userId = 1L;
        String accessToken = "test.access.token";
        String refreshToken = "test.refresh.token";

        when(jwtUtil.createAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(userId)).thenReturn(refreshToken);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(null);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        // when & then
        assertThrows(RuntimeException.class, ()
                -> tokenService.createTokens(userId)
        );
    }

    @Test
    @DisplayName("리프레시 토큰 갱신 실패 - 리포지토리 업데이트 오류")
    void createTokens_Failure_UpdateError() {
        // given
        Long userId = 1L;
        String accessToken = "test.access.token";
        String refreshToken = "test.refresh.token";

        RefreshToken existingToken = RefreshToken.builder()
                .user(User.builder().id(userId).build())
                .token("old.refresh.token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtUtil.createAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(userId)).thenReturn(refreshToken);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(existingToken);
        doThrow(new RuntimeException("DB 업데이트 실패"))
                .when(refreshTokenRepository).updateRefreshToken(any(RefreshToken.class));

        // when & then
        assertThrows(RuntimeException.class, ()
                -> tokenService.createTokens(userId)
        );
    }


    // 액세스 토큰 갱신 테스트
    @Test
    @DisplayName("액세스 토큰 갱신 성공")
    void refreshAccessToken_Success() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;
        String newAccessToken = "new.access.token";

        RefreshToken savedToken = RefreshToken.builder()
                .token(refreshToken)
                .user(User.builder().id(userId).build())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(jwtUtil.createAccessToken(userId)).thenReturn(newAccessToken);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(savedToken);

        // when
        TokenResponseDTO result = tokenService.refreshAccessToken(refreshToken);

        // then
        assertNotNull(result);
        assertEquals(newAccessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
        verify(jwtUtil).createAccessToken(userId);
    }

    @Test
    @DisplayName("액세스 토큰 갱신 실패 - 유효하지 않은 리프레시 토큰")
    void refreshAccessToken_Failure_InvalidToken() {
        // given
        String refreshToken = "invalid.refresh.token";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        // when & then
        assertThrows(InvalidTokenException.class, ()
                -> tokenService.refreshAccessToken(refreshToken)
        );
        verify(jwtUtil).validateToken(refreshToken);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("액세스 토큰 갱신 실패 - DB에 저장된 토큰 없음")
    void refreshAccessToken_Failure_TokenNotFound() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(null);

        // when & then
        assertThrows(InvalidTokenException.class, ()
                -> tokenService.refreshAccessToken(refreshToken)
        );
        verify(refreshTokenRepository).findByUser_Id(userId);
    }

    @Test
    @DisplayName("액세스 토큰 갱신 실패 - 토큰 불일치")
    void refreshAccessToken_Failure_TokenMismatch() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;

        RefreshToken savedToken = RefreshToken.builder()
                .token("different.refresh.token")
                .user(User.builder().id(userId).build())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(savedToken);

        // when & then
        assertThrows(InvalidTokenException.class, ()
                -> tokenService.refreshAccessToken(refreshToken)
        );
    }

    @Test
    @DisplayName("액세스 토큰 갱신 실패 - 만료된 리프레시 토큰")
    void refreshAccessToken_Failure_ExpiredToken() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;

        RefreshToken savedToken = RefreshToken.builder()
                .token(refreshToken)
                .user(User.builder().id(userId).build())
                .expiryDate(LocalDateTime.now().minusDays(1)) // 만료된 토큰
                .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(savedToken);

        // when & then
        assertThrows(InvalidTokenException.class, ()
                -> tokenService.refreshAccessToken(refreshToken)
        );
        verify(refreshTokenRepository).deleteByUser_Id(userId);  // 만료된 토큰은 삭제되어야 함
    }


    // 리프레시 토큰 폐기 테스트
    @Test
    @DisplayName("리프레시 토큰 폐기 성공")
    void revokeRefreshToken_Success() {
        // given
        Long userId = 1L;

        // when
        tokenService.revokeRefreshToken(userId);

        // then
        verify(refreshTokenRepository).deleteByUser_Id(userId);
    }

    @Test
    @DisplayName("리프레시 토큰 폐기 실패")
    void revokeRefreshToken_Failure() {
        // given
        Long userId = 1L;
        doThrow(new RuntimeException("DB 삭제 실패"))
                .when(refreshTokenRepository).deleteByUser_Id(userId);

        // when & then
        assertThrows(RuntimeException.class, ()
                -> tokenService.revokeRefreshToken(userId)
        );
        verify(refreshTokenRepository).deleteByUser_Id(userId);
    }

    @Test
    @DisplayName("리프레시 토큰 폐기 실패 - 리포지토리 예외")
    void revokeRefreshToken_Failure_RepositoryException() {
        // given
        Long userId = 1L;
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(refreshTokenRepository).deleteByUser_Id(userId);

        // when & then
        assertThrows(RuntimeException.class, ()
                -> tokenService.revokeRefreshToken(userId)
        );
        verify(refreshTokenRepository).deleteByUser_Id(userId);
    }


    // 리프레시 토큰 유효성 검사 테스트
    @Test
    @DisplayName("리프레시 토큰 유효성 검사 성공")
    void validateRefreshToken_Success() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;

        RefreshToken savedToken = RefreshToken.builder()
                .token(refreshToken)
                .user(User.builder().id(userId).build())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(savedToken);

        // when
        boolean result = tokenService.validateRefreshToken(refreshToken);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("리프레시 토큰 유효성 검사 실패")
    void validateRefreshToken_Failure() {
        // given
        String refreshToken = "invalid.refresh.token";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        // when
        boolean result = tokenService.validateRefreshToken(refreshToken);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 - userId 추출 실패")
    void validateRefreshToken_Failure_InvalidUserId() {
        // given
        String refreshToken = "invalid.user.token";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenThrow(new InvalidTokenException("유효하지 않은 userId"));

        // when & then
        assertFalse(tokenService.validateRefreshToken(refreshToken));
        verify(jwtUtil).validateToken(refreshToken);
        verify(jwtUtil).getUserId(refreshToken);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 - 토큰 형식 오류")
    void validateRefreshToken_Failure_MalformedToken() {
        // given
        String refreshToken = "malformed.token";
        when(jwtUtil.validateToken(refreshToken))
                .thenThrow(new InvalidTokenException("잘못된 토큰 형식"));

        // when
        boolean result = tokenService.validateRefreshToken(refreshToken);

        // then
        assertFalse(result);
        verify(jwtUtil).validateToken(refreshToken);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 - 예기치 않은 예외")
    void validateRefreshToken_Failure_UnexpectedException() {
        // given
        String refreshToken = "test.refresh.token";
        when(jwtUtil.validateToken(refreshToken))
                .thenThrow(new InvalidTokenException("예기치 않은 오류"));

        // when
        boolean result = tokenService.validateRefreshToken(refreshToken);

        // then
        assertFalse(result);
        verify(jwtUtil).validateToken(refreshToken);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 - DB 조회 오류")
    void validateRefreshToken_Failure_DatabaseError() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUser_Id(userId))
                .thenThrow(new InvalidTokenException("DB 조회 오류"));

        // when
        boolean result = tokenService.validateRefreshToken(refreshToken);

        // then
        assertFalse(result);
        verify(jwtUtil).validateToken(refreshToken);
        verify(jwtUtil).getUserId(refreshToken);
        verify(refreshTokenRepository).findByUser_Id(userId);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 - 토큰 불일치")
    void validateRefreshToken_Failure_TokenMismatch() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;
        
        RefreshToken savedToken = RefreshToken.builder()
            .token("different.refresh.token")
            .user(User.builder().id(userId).build())
            .expiryDate(LocalDateTime.now().plusDays(7))
            .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(savedToken);

        // when
        boolean result = tokenService.validateRefreshToken(refreshToken);

        // then
        assertFalse(result);
        verify(jwtUtil).validateToken(refreshToken);
        verify(jwtUtil).getUserId(refreshToken);
        verify(refreshTokenRepository).findByUser_Id(userId);
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 - 토큰 조회 결과 없음")
    void validateRefreshToken_Failure_TokenNotFound() {
        // given
        String refreshToken = "test.refresh.token";
        Long userId = 1L;

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.findByUser_Id(userId)).thenReturn(null);

        // when
        boolean result = tokenService.validateRefreshToken(refreshToken);

        // then
        assertFalse(result);
        verify(jwtUtil).validateToken(refreshToken);
        verify(jwtUtil).getUserId(refreshToken);
        verify(refreshTokenRepository).findByUser_Id(userId);
    }
}
