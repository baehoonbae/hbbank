package com.hbbank.backend.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 로그인 시작 - Provider: {}", userRequest.getClientRegistration().getRegistrationId());
        
        OAuth2User oauth2User = super.loadUser(userRequest);
        log.debug("OAuth2 사용자 정보 로드 완료");
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        log.debug("OAuth2 사용자 정보 - 이메일: {}, 이름: {}", email, name);
        
        // 사용자 정보로 회원가입 또는 로그인 처리
        userService.findByEmail(email)
            .orElseGet(() -> {
                log.info("신규 OAuth2 사용자 등록 - 이메일: {}", email);
                return userService.registOAuth2User(email, name);
            });
            
        log.info("OAuth2 로그인 완료 - 이메일: {}", email);
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            oauth2User.getAttributes(),
            "email"
        );
    }
}
