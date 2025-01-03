package com.hbbank.backend.service;

import java.time.LocalDateTime;

import com.hbbank.backend.dto.RefreshTokenDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.RefreshToken;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.TokenResponseDTO;
import com.hbbank.backend.exception.token.InvalidTokenException;
import com.hbbank.backend.repository.RefreshTokenRepository;
import com.hbbank.backend.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponseDTO createTokens(Long userId) {
        String accessToken = jwtUtil.createAccessToken(userId);
        String refreshToken = jwtUtil.createRefreshToken(userId);
        saveOrUpdateRefreshToken(userId, refreshToken);

        log.info("새로운 토큰 발급 완료 - 사용자ID: {}", userId);
        return new TokenResponseDTO(accessToken, refreshToken);
    }

    private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(User.builder().id(userId).build())
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(14))
                .build();

        RefreshToken existingToken = refreshTokenRepository.findByUser_Id(userId);
        if (existingToken != null) {
            refreshTokenRepository.updateRefreshToken(refreshTokenEntity);
            log.info("리프레시 토큰 갱신 - 사용자ID: {}", userId);
        } else {
            refreshTokenRepository.save(refreshTokenEntity);
            log.info("새로운 리프레시 토큰 저장 - 사용자ID: {}", userId);
        }
    }

    public TokenResponseDTO refreshAccessToken(String refreshToken) {
        validateRefreshTokenOrThrow(refreshToken);
        Long userId = jwtUtil.getUserId(refreshToken);
        String newAccessToken = jwtUtil.createAccessToken(userId);

        log.info("액세스 토큰 재발급 완료 - 사용자ID: {}", userId);
        return new TokenResponseDTO(newAccessToken, refreshToken);
    }

    private void validateRefreshTokenOrThrow(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            log.error("리프레시 토큰 검증 실패 - 유효하지 않은 토큰");
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByUser_Id(userId);

        if (savedToken == null || !savedToken.getToken().equals(refreshToken)) {
            log.error("리프레시 토큰 검증 실패 - 저장된 토큰과 불일치 (사용자ID: {})", userId);
            throw new InvalidTokenException("저장된 리프레시 토큰과 불일치");
        }

        if (savedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.error("리프레시 토큰 검증 실패 - 만료된 토큰 (사용자ID: {})", userId);
            refreshTokenRepository.deleteByUser_Id(userId);
            throw new InvalidTokenException("만료된 리프레시 토큰");
        }
    }

    public void revokeRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
        log.info("리프레시 토큰 폐기 완료 - 사용자ID: {}", userId);
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            validateRefreshTokenOrThrow(refreshToken);
            return true;
        } catch (InvalidTokenException e) {
            log.warn("리프레시 토큰 유효성 검사 실패 - 사유: {}", e.getMessage());
            return false;
        }
    }

    public String verifyAccessToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidBearerTokenException("유효하지 않은 접근입니다.");
        }
        return header.substring(7);
    }

    public void verifyRefreshToken(RefreshTokenDTO request){
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            throw new InvalidTokenException("리프레시 토큰이 필요합니다.");
        }
    }
}
