package com.gogym.member.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final List<String> exemptUrls;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // 인증 제외 경로에 해당하면 필터를 건너뜁니다.
    return exemptUrls.stream().anyMatch(path::startsWith);
    
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    
    try {
      // 요청 헤더에서 JWT 토큰 추출
      String token = jwtTokenProvider.extractToken(request, null);

      // 토큰 검증 및 SecurityContext 설정
      if (token != null && jwtTokenProvider.validateToken(token)) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        
        if (authentication != null) {
          // SecurityContextHolder에 인증 정보 설정
          UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
              authentication.getPrincipal(),
              null,
              authentication.getAuthorities()
              );
          
          authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          }
        
      }
      
    } catch (Exception e) {
      // 예외 발생 시 SecurityContext를 초기화하여 보호
      SecurityContextHolder.clearContext();
      
    }
    // 다음 필터 체인으로 요청 전달
    filterChain.doFilter(request, response);
    }
}


