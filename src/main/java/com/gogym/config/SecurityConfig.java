package com.gogym.config;

import com.gogym.config.JwtAuthenticationFilter;
import com.gogym.config.JwtTokenProvider;
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

  public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  /**
   * PasswordEncoder Bean 등록
   * 비밀번호 암호화를 위한 BCryptPasswordEncoder 사용
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * AuthenticationManager Bean 등록
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  /**
   * SecurityFilterChain 설정
   * JWT 필터 추가 및 특정 URL 접근 허용 설정
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable() // CSRF 비활성화
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/members/sign-up",
                "/api/members/sign-in",
                "/api/members/check-email",
                "/api/members/check-nickname",
                "/api/members/verify-code",
                "/api/members/send-code"
            ).permitAll() // 인증 없이 접근 허용
            .anyRequest().authenticated() // 나머지 요청은 인증 필요
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가
    return http.build();
  }
}

