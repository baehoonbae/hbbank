package com.hbbank.backend.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.LoginRequestDTO;
import com.hbbank.backend.dto.LoginResponseDTO;
import com.hbbank.backend.dto.RefreshTokenDTO;
import com.hbbank.backend.dto.TokenResponseDTO;
import com.hbbank.backend.exception.InvalidTokenException;
import com.hbbank.backend.service.TokenService;
import com.hbbank.backend.service.UserService;
import com.hbbank.backend.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    // 회원가입
    @PostMapping("/regist")
    public ResponseEntity<?> regist(@RequestBody User user) {
        User registeredUser = userService.regist(user);
        if (registeredUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "회원가입이 완료되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "회원가입에 실패했습니다."));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        log.info("loginRequest: {}", loginRequest);
        try {
            Optional<User> opUser = userService.login(loginRequest);
            return opUser
                    .map(user -> {
                        TokenResponseDTO tokens = tokenService.createTokens(user.getId());

                        ResponseCookie refreshTokenCookie = ResponseCookie
                                .from("refreshToken", tokens.getRefreshToken())
                                .httpOnly(true) // 자바스크립트 접근 불가
                                .secure(true) // https 프로토콜에서만 전송(개발 단계에서는 주석 처리)
                                .path("/") // 모든 경로에서 접근 가능
                                .maxAge(60 * 60 * 24 * 14) // 2주
                                .sameSite("None") // https 필수
                                // .domain("fqdashboard.duckdns.org") // 도메인 설정(생기면 주석 해제)
                                .build();

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                .body(new LoginResponseDTO(
                                        tokens.getAccessToken(),
                                        user.getId(),
                                        user.getUsername(),
                                        user.getName(),
                                        "로그인 성공!"));
                    })
                    .orElse(ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponseDTO(null, null, null, null, "로그인 실패")));
        } catch (Exception e) {
            log.error("Login error: ", e); // 상세 로그 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponseDTO(null, null, null, null, "서버 오류"));
        }
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO request) {
        try {
            if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("리프레시 토큰이 필요합니다.");
            }

            TokenResponseDTO tokens = tokenService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (InvalidTokenException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("토큰 갱신 중 오류가 발생했습니다.");
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        log.info("bearerToken: {}", bearerToken);
        try {
            String token = bearerToken.substring(7); // "Bearer " 제거
            Long userId = jwtUtil.getUserId(token);
            tokenService.revokeRefreshToken(userId);
        } catch (Exception e) {
            log.warn("Invalid token during logout: {}", e.getMessage());
        }

        ResponseCookie clear = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                // .domain("fqdashboard.duckdns.org")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .body("로그아웃 성공");
    }
}
