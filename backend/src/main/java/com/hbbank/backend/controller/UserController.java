package com.hbbank.backend.controller;

import java.net.URI;

import com.hbbank.backend.dto.*;
import org.springframework.http.HttpHeaders;
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

import com.hbbank.backend.domain.User;
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
    public ResponseEntity<LoginResponseDTO> me(@RequestHeader("Authorization") String header) {
        String accessToken = tokenService.verifyAccessToken(header);
        User user = userService.findById(jwtUtil.getUserId(accessToken));

        return ResponseEntity
                .ok(LoginResponseDTO.from(accessToken, user));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegistDTO dto) {
        User user = userService.register(dto);
        return ResponseEntity
                .created(URI.create("/api/user/signup/" + user.getId()))
                .body(user);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("로그인 시작 - 요청 정보: {}", loginRequest);
        User user = userService.login(loginRequest);
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

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(LoginResponseDTO.from(tokens.getAccessToken(), user));
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken(@RequestBody RefreshTokenDTO request) {
        tokenService.verifyRefreshToken(request);
        return ResponseEntity
                .ok(tokenService.refreshAccessToken(request.getRefreshToken()));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String bearerToken) {
        log.info("bearerToken: {}", bearerToken);

        String token = bearerToken.substring(7); // "Bearer " 제거
        tokenService.revokeRefreshToken(jwtUtil.getUserId(token));

        ResponseCookie clear = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                // .domain("fqdashboard.duckdns.org")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .body("로그아웃 성공");
    }

    // 인증 메일 발송 요청
    @PostMapping("/email/send")
    public ResponseEntity<EmailResponseDTO> sendVerificationEmail(@RequestParam String email) {
        emailService.sendVerificationEmail(email);
        return ResponseEntity
                .ok(new EmailResponseDTO("인증 메일이 발송되었습니다."));
    }

    // 인증 코드 확인
    @PostMapping("/email/verify")
    public ResponseEntity<EmailResponseDTO> verifyEmail(@RequestParam String email, @RequestParam String code) {
        emailService.verifyEmail(email, code);
        userService.completeEmailVerification(email);
        return ResponseEntity
                .ok(new EmailResponseDTO("이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/oauth2/additional-info")
    public ResponseEntity<AdditionalInfoResponseDTO> updateOAuth2UserInfo(@AuthenticationPrincipal Long userId, @Valid @RequestBody OAuth2AdditionalInfoDTO dto) {
        userService.updateAdditionalInfo(userId, dto);
        return ResponseEntity
                .ok(new AdditionalInfoResponseDTO("추가 정보가 성공적으로 저장되었습니다."));
    }
}
