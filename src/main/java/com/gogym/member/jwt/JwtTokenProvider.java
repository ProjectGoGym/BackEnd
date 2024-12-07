package com.gogym.member.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long validityInMilliseconds;

  // JwtTokenProvider 생성자
  public JwtTokenProvider(@Value("${spring.jwt.secret}") String secret,
      @Value("${spring.jwt.validity}") long validityInMilliseconds) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.validityInMilliseconds = validityInMilliseconds;
  }

  // JWT 생성
  public String createToken(String email, Long memberId, List<String> roles) {
    Claims claims = Jwts.claims().setSubject(email); // 사용자 이메일 설정
    claims.put("id", memberId); // 사용자 ID 추가
    claims.put("roles", roles); // 사용자 권한 추가

    Date now = new Date();
    Date expiration = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder().setClaims(claims) // 클레임 설정
        .setIssuedAt(now) // 발급 시간
        .setExpiration(expiration) // 만료 시간
        .signWith(secretKey, SignatureAlgorithm.HS256) // 서명 알고리즘 및 비밀 키
        .compact();

  }

  // id 추출
  public Long extractMemberId(String token) {
    Claims claims = getClaims(token);
    return Long.valueOf(claims.get("id").toString());
  }

  // JWT에서 인증 정보 추출
  public Authentication getAuthentication(String token) {
    Claims claims = getClaims(token); // 토큰에서 클레임 추출

    // 클레임에서 사용자 권한 정보 추출
    List<String> roles = (List<String>) claims.get("roles");
    List<SimpleGrantedAuthority> authorities =
        roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

    // 사용자 정보 설정
    User principal = new User(claims.getSubject(), "", authorities);

    return new UsernamePasswordAuthenticationToken(principal, null, authorities);
  }

  // 토큰에서 클레임 추출
  private Claims getClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(secretKey) // 서명 키 설정
        .build().parseClaimsJws(token) // 토큰 파싱
        .getBody();
  }

  // 유효성 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey) // 서명 키 설정
          .build().parseClaimsJws(token); // 토큰 검증
      return true;
    } catch (Exception e) {
      return false; // 유효하지 않은 토큰
    }
  }

  // JWT 추출
  public String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // "Bearer "를 제외하고 토큰 반환
    }
    return null; // 헤더가 없거나 잘못된 형식인 경우 null 반환

  }

}
