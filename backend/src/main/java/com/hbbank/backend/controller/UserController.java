package com.hbbank.backend.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.LoginRequestDTO;
import com.hbbank.backend.dto.LoginResponseDTO;
import com.hbbank.backend.dto.OAuth2AdditionalInfoDTO;
import com.hbbank.backend.dto.RefreshTokenDTO;
import com.hbbank.backend.dto.TokenResponseDTO;
import com.hbbank.backend.dto.UserRegistDTO;
import com.hbbank.backend.exception.InvalidTokenException;
import com.hbbank.backend.service.EmailService;
import com.hbbank.backend.service.TokenService;
import com.hbbank.backend.service.UserService;
import com.hbbank.backend.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 없거나 유효하지 않습니다.");
        }

        String token = header.substring(7);
        Long userId = jwtUtil.getUserId(token);
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"));

        return ResponseEntity.ok(new LoginResponseDTO(token, user.getId(), user.getUsername(), user.getName(), "로그인 성공!"));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> regist(@Valid @RequestBody UserRegistDTO dto) {
        User registeredUser = userService.regist(dto);
        if (registeredUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "회원가입이 완료되었습니다."));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "회원가입에 실패했습니다."));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
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

        String token = bearerToken.substring(7); // "Bearer " 제거
        Long userId = jwtUtil.getUserId(token);
        tokenService.revokeRefreshToken(userId);

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

    // 인증 메일 발송 요청
    @PostMapping("/email/send")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) {
        emailService.sendVerificationEmail(email);
        return ResponseEntity.ok()
                .body(Map.of("message", "인증 코드가 발송되었습니다."));
    }

    // 인증 코드 확인
    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String code) {
        if (emailService.verifyEmail(email, code)) {
            userService.completeEmailVerification(email);
            return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
        }
        return ResponseEntity.badRequest()
                .body(Map.of("message", "잘못된 인증 코드입니다."));
    }

    @PostMapping("/oauth2/additional-info")
    public ResponseEntity<?> updateOAuth2UserInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OAuth2AdditionalInfoDTO dto
    ) {
        try {
            userService.updateAdditionalInfo(userId, dto);
            return ResponseEntity.ok(Map.of("message", "추가 정보가 성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "추가 정보 저장에 실패했습니다."));
        }
    }
}
