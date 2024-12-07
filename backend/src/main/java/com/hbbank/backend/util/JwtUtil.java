package com.hbbank.backend.util;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final String secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("Secret key must be at least 256 bits long");
        }
        this.secretKey = secretKey;
    }

    private final long accessTokenExpireTime = 1000 * 60 * 30; // 30분
    private final long refreshTokenExpireTime = 1000 * 60 * 60 * 24 * 14; // 2주

    // 액세스 토큰 생성
    public String createAccessToken(Long userId) {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("type", "access");
        claims.put("timestamp", System.currentTimeMillis()); // 생성 시간 추가

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("access_token")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpireTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // 리프레시 토큰 생성
    public String createRefreshToken(Long userId) {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        claims.put("timestamp", System.currentTimeMillis()); // 생성 시간 추가
        claims.put("random", generateRandomString()); // 랜덤 문자열 추가

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("refresh_token")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpireTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 랜덤 문자열 생성 메서드 추가
    private String generateRandomString() {
        byte[] array = new byte[7]; // 랜덤 바이트 배열 생성
        new SecureRandom().nextBytes(array);
        return Base64.getEncoder().encodeToString(array);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                    
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());  // 만료 시간 검증
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰 타입 검증 메서드 추가
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
}