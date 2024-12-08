package com.hbbank.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.RefreshToken;
import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.TokenResponseDTO;
import com.hbbank.backend.exception.InvalidTokenException;
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

		log.debug("accessToken: {}", accessToken);
		log.debug("refreshToken: {}", refreshToken);

		saveOrUpdateRefreshToken(userId, refreshToken);
		return new TokenResponseDTO(accessToken, refreshToken);
	}

	private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
		RefreshToken refreshTokenEntity = RefreshToken.builder()
			.user(User.builder().id(userId).build())
			.token(refreshToken)
			.expiryDate(LocalDateTime.now().plusDays(14))
			.build();

		log.debug("refreshTokenEntity: {}", refreshTokenEntity);
		RefreshToken existingToken = refreshTokenRepository.findByUser_Id(userId);
		log.debug("existingToken: {}", existingToken);
		if (existingToken != null) {
			refreshTokenRepository.updateRefreshToken(refreshTokenEntity);
		} else {
			refreshTokenRepository.save(refreshTokenEntity);
		}
	}

	public TokenResponseDTO refreshAccessToken(String refreshToken) {
		validateRefreshTokenOrThrow(refreshToken);
		Long userId = jwtUtil.getUserId(refreshToken);
		String newAccessToken = jwtUtil.createAccessToken(userId);

		return new TokenResponseDTO(newAccessToken, refreshToken);
	}

	private void validateRefreshTokenOrThrow(String refreshToken) {
		if (!jwtUtil.validateToken(refreshToken)) {
			throw new InvalidTokenException("유효하지 않은 리프레시 토큰");
		}

		Long userId = jwtUtil.getUserId(refreshToken);
		RefreshToken savedToken = refreshTokenRepository.findByUser_Id(userId);

		if (savedToken == null || !savedToken.getToken().equals(refreshToken)) {
			throw new InvalidTokenException("저장된 리프레시 토큰과 불일치");
		}

		if (savedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			refreshTokenRepository.deleteByUser_Id(userId);
			throw new InvalidTokenException("만료된 리프레시 토큰");
		}
	}

	public void revokeRefreshToken(Long userId) {
		refreshTokenRepository.deleteByUser_Id(userId);
	}

	public boolean validateRefreshToken(String refreshToken) {
		try {
			validateRefreshTokenOrThrow(refreshToken);
			return true;
		} catch (InvalidTokenException e) {
			return false;
		}
	}
}
