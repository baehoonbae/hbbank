package com.hbbank.backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.TokenResponseDTO;
import com.hbbank.backend.service.OAuth2UserService;
import com.hbbank.backend.service.TokenService;
import com.hbbank.backend.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TokenService tokenService;
    private final OAuth2UserService oauth2UserService;

    public SecurityConfig(
            TokenService tokenService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OAuth2UserService oauth2UserService) {
        this.tokenService = tokenService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oauth2UserService = oauth2UserService;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
        return web -> web.ignoring()
                // error endpoint를 열어줘야 함, favicon.ico 추가!
                .requestMatchers("/error", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                // 인증이 필요한 작업들
                .requestMatchers(
                        "/api/user/**",
                        "/api/user/oauth2/**",
                        "/oauth2/**",
                        "/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler())
                .failureHandler(oAuth2AuthenticationFailureHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 특정 패턴으로 시작하는 도메인 허용
        configuration.setAllowedOriginPatterns(Arrays.asList(
                // 이후 실제로 배포될 도메인으로 변경
                // "https://fqdashboard.netlify.app",
                // "https://fqdashboard.duckdns.org",
                "https://3.24.232.172",
                "http://localhost:5173",
                "http://localhost:8080"));

        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 인증 정보 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        // 노출할 헤더 설정
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            User user = userService.findByEmail(email);
            TokenResponseDTO tokens = tokenService.createTokens(user.getId());

            String redirectUrl = UriComponentsBuilder
                    .fromUriString("http://localhost:5173/oauth2/redirect")
                    .queryParam("token", tokens.getAccessToken())
                    .queryParam("needAdditionalInfo", user.isNeedAdditionalInfo())
                    .build().toUriString();

            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.sendRedirect("/oauth2/failure");  // 프론트엔드 실패 페이지 URL
        };
    }
}
