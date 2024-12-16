package com.gogym.member.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final List<String> exemptUrls;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();

    // WebSocket 초기 연결 요청 시에는 Interceptor에서 확인
    if (path.startsWith("/ws")) {
      return true;
    }

    // 인증이 필요 없는 경로 확인
    return exemptUrls.contains(path);
  }
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String uri = request.getRequestURI();

    if (exemptUrls.contains(uri)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = jwtTokenProvider.extractToken(request);

    try {
      if (token != null && jwtTokenProvider.validateToken(token)) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        if (authentication != null) {
          SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
          securityContext.setAuthentication(authentication);
          SecurityContextHolder.setContext(securityContext);
        }
      } else {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return; // 예외 발생 시 필터 체인 종료
      }
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return; // 예외 발생 시 필터 체인 종료
    }

    // 인증 성공 시 다음 필터로 진행
    filterChain.doFilter(request, response);
  }
}
