package com.hbbank.backend.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hbbank.backend.exception.InvalidTokenException;
import com.hbbank.backend.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
                                      
        // 토큰이 필요없는 경로는 제외
        if (isPermitAllUrl(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        
        try {
            if (token != null) {
                // 토큰 유효성 검사
                if (!jwtUtil.validateToken(token)) {
                    throw new InvalidTokenException("만료되거나 유효하지 않은 토큰입니다");
                }
                
                // 유저 ID 추출
                Long userId = jwtUtil.getUserId(token);
                
                // SecurityContext에 인증 정보 설정
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
            
        } catch (InvalidTokenException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPermitAllUrl(String requestURI) {
        return Arrays.asList(
            "/api/user/login",
            "/api/user/regist",
            "/api/user/check-refresh-token",
            "/api/user/refresh",
            "/api/user/logout"
        ).contains(requestURI);
    }
} 