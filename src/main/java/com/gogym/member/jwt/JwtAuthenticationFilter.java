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
    // 인증이 필요 없는 경로 확인
    return exemptUrls.contains(path);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    
    // 요청 헤더에서 JWT 토큰 추출
    String token = jwtTokenProvider.extractToken(request);

    try {
      // 토큰 유효성 검증
      if (token != null && jwtTokenProvider.validateToken(token)) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        if (authentication != null) {
          UsernamePasswordAuthenticationToken authenticationToken =
              new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), null,
                  authentication.getAuthorities());

          authenticationToken
              .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
      } else if (token == null) {
        logger.warn("Authorization 헤더가 누락되었습니다.");
        // SecurityContextHolder.clearContext();
        // return;
      } else {
        logger.warn("유효하지 않은 토큰입니다.");
      }

    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      logger.error("JWT 인증 과정에서 예외 발생", e);
    }
    // 인증이 실패했더라도 다음 필터로 전달
    filterChain.doFilter(request, response);

  }
}
