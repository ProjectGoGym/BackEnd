package com.gogym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;

  /**
   * SecurityConfig 생성자
   *
   * @param jwtTokenProvider JWT 관련 기능 제공 클래스
   */
  public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  /**
   * 비밀번호 암호화를 위한 PasswordEncoder Bean 등록
   *
   * @return BCryptPasswordEncoder 객체
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * AuthenticationManager Bean 등록
   *
   * @param configuration AuthenticationConfiguration 객체
   * @return AuthenticationManager 객체
   * @throws Exception 예외 발생 시
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  /**
   * Spring Security 설정 및 SecurityFilterChain Bean 등록
   *
   * - 특정 URL 접근 허용 설정
   * - JWT 인증 필터 추가
   *
   * @param http HttpSecurity 객체
   * @return SecurityFilterChain 객체
   * @throws Exception 예외 발생 시
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 비활성화 (REST API 사용 시 필요 없음)
        .csrf().disable()
        .authorizeHttpRequests(auth -> auth
            // 인증 없이 접근을 허용할 엔드포인트
            .requestMatchers(
                "/api/members/sign-up",
                "/api/members/sign-in",
                "/api/members/check-email",
                "/api/members/check-nickname",
                "/api/members/verify-code",
                "/api/members/send-code"
            ).permitAll()
            // 그 외의 모든 요청은 인증 필요
            .anyRequest().authenticated()
        )
        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}


