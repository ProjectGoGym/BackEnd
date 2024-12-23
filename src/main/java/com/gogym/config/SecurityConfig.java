package com.gogym.config;

import com.gogym.member.jwt.JwtAuthenticationFilter;
import com.gogym.member.jwt.JwtTokenProvider;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;

  // SecurityConfig 생성자
  public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  // 비밀번호 암호화를 위한 PasswordEncoder Bean 등록
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // AuthenticationManager Bean 등록
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  // SecurityFilterChain Bean 등록
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
    .csrf(csrf -> csrf.disable()) // CSRF disable
    .cors(Customizer.withDefaults()) // CORS 설정
    .authorizeHttpRequests(auth -> auth
            // 인증 없이 접근을 허용할 엔드포인트
            .requestMatchers("/api/auth/sign-up", "/api/auth/sign-in", "/api/auth/check-email",
                "/api/auth/check-nickname", "/api/auth/verify-email", "/api/auth/reset-password",
                "/api/auth/send-verification-email", "/api/regions", "/api/kakao/sign-in/**",
                "/api/posts/views", "/api/posts/filters", "/api/posts/details/**",
                "/api/payments/webhook",
                "/api/payments/sse/subscribe/**", "/api/images/presigned-url", "/ws/**",
                "/api/notifications/subscribe/**")
            .permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // 그 외의 모든 요청은 인증 필요
            .anyRequest().authenticated())
        // JWT 인증 필터를 AuthenticationFilter 전에 추가
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // JwtAuthenticationFilter Bean
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtTokenProvider, exemptUrls());
  }

  // 인증 제외 경로
  private List<String> exemptUrls() {
    return List.of("/api/auth/sign-up", "/api/auth/sign-in", "/api/auth/check-email",
        "/api/auth/check-nickname", "/api/auth/verify-email", "/api/auth/reset-password",
        "/api/auth/send-verification-email", "/api/regions", "/api/kakao/sign-in/**",
        "/api/posts/views", "/api/posts/filters", "/api/posts/details/**", "/api/payments/webhook",
        "/api/payments/sse/subscribe/**", "/api/images/presigned-url", "/ws/**",
        "/api/notifications/subscribe/**");
  }
}
