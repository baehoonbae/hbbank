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
		log.info("토큰 생성 시작 - 사용자ID: {}", userId);
		
		String accessToken = jwtUtil.createAccessToken(userId);
		String refreshToken = jwtUtil.createRefreshToken(userId);

		log.debug("토큰 생성 완료 - accessToken: {}, refreshToken: {}", accessToken, refreshToken);

		saveOrUpdateRefreshToken(userId, refreshToken);
		
		log.info("토큰 생성 및 저장 완료 - 사용자ID: {}", userId);
		return new TokenResponseDTO(accessToken, refreshToken);
	}

	private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
		log.debug("리프레시 토큰 저장/갱신 시작 - 사용자ID: {}", userId);
		
		RefreshToken refreshTokenEntity = RefreshToken.builder()
			.user(User.builder().id(userId).build())
			.token(refreshToken)
			.expiryDate(LocalDateTime.now().plusDays(14))
			.build();

		log.debug("리프레시 토큰 엔티티 생성 - {}", refreshTokenEntity);
		RefreshToken existingToken = refreshTokenRepository.findByUser_Id(userId);
		log.debug("기존 리프레시 토큰 조회 결과: {}", existingToken);
		
		if (existingToken != null) {
			log.debug("기존 리프레시 토큰 갱신 - 사용자ID: {}", userId);
			refreshTokenRepository.updateRefreshToken(refreshTokenEntity);
		} else {
			log.debug("새 리프레시 토큰 저장 - 사용자ID: {}", userId);
			refreshTokenRepository.save(refreshTokenEntity);
		}
		log.debug("리프레시 토큰 저장/갱신 완료 - 사용자ID: {}", userId);
	}

	public TokenResponseDTO refreshAccessToken(String refreshToken) {
		log.info("액세스 토큰 갱신 시작 - refreshToken: {}", refreshToken);
		
		validateRefreshTokenOrThrow(refreshToken);
		Long userId = jwtUtil.getUserId(refreshToken);
		String newAccessToken = jwtUtil.createAccessToken(userId);

		log.info("액세스 토큰 갱신 완료 - 사용자ID: {}", userId);
		return new TokenResponseDTO(newAccessToken, refreshToken);
	}

	private void validateRefreshTokenOrThrow(String refreshToken) {
		log.debug("리프레시 토큰 검증 시작 - token: {}", refreshToken);
		
		if (!jwtUtil.validateToken(refreshToken)) {
			log.error("리프레시 토큰 검증 실패 - 유효하지 않은 토큰");
			throw new InvalidTokenException("유효하지 않은 리프레시 토큰");
		}

		Long userId = jwtUtil.getUserId(refreshToken);
		RefreshToken savedToken = refreshTokenRepository.findByUser_Id(userId);
		log.debug("저장된 리프레시 토큰 조회 - 사용자ID: {}, token: {}", userId, savedToken);

		if (savedToken == null || !savedToken.getToken().equals(refreshToken)) {
			log.error("리프레시 토큰 검증 실패 - 토큰 불일치 (사용자ID: {})", userId);
			throw new InvalidTokenException("저장된 리프레시 토큰과 불일치");
		}

		if (savedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			log.error("리프레시 토큰 검증 실패 - 만료된 토큰 (사용자ID: {})", userId);
			refreshTokenRepository.deleteByUser_Id(userId);
			throw new InvalidTokenException("만료된 리프레시 토큰");
		}
		
		log.debug("리프레시 토큰 검증 완료 - 사용자ID: {}", userId);
	}

	public void revokeRefreshToken(Long userId) {
		log.info("리프레시 토큰 폐기 시작 - 사용자ID: {}", userId);
		refreshTokenRepository.deleteByUser_Id(userId);
		log.info("리프레시 토큰 폐기 완료 - 사용자ID: {}", userId);
	}

	public boolean validateRefreshToken(String refreshToken) {
		log.debug("리프레시 토큰 유효성 검사 시작 - token: {}", refreshToken);
		try {
			validateRefreshTokenOrThrow(refreshToken);
			log.debug("리프레시 토큰 유효성 검사 성공");
			return true;
		} catch (InvalidTokenException e) {
			log.warn("리프레시 토큰 유효성 검사 실패 - 사유: {}", e.getMessage());
			return false;
		}
	}
}
