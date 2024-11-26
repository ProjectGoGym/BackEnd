package com.gogym.config;

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

  /**
   * JwtTokenProvider 생성자
   * @param secret 비밀 키
   * @param validityInMilliseconds 토큰 유효 시간 (밀리초)
   */
  public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                          @Value("${jwt.validity}") long validityInMilliseconds) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.validityInMilliseconds = validityInMilliseconds;
  }

  /**
   * JWT 생성
   * @param username 사용자 이름 (혹은 이메일)
   * @param roles 사용자 권한 목록
   * @return 생성된 JWT 문자열
   */
  public String createToken(String username, List<String> roles) {
    Claims claims = Jwts.claims().setSubject(username); // sub 필드에 사용자 이름 설정
    claims.put("roles", roles); // 사용자 권한을 클레임에 추가

    Date now = new Date();
    Date expiration = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now) // 발급 시간
        .setExpiration(expiration) // 만료 시간
        .signWith(secretKey, SignatureAlgorithm.HS256) // 비밀 키와 알고리즘 설정
        .compact();
  }

  /**
   * JWT에서 인증 정보 추출
   * @param token JWT 토큰
   * @return Authentication 객체
   */
  public Authentication getAuthentication(String token) {
    Claims claims = getClaims(token);

    // 클레임에서 권한 정보 추출
    List<String> roles = (List<String>) claims.get("roles");
    List<SimpleGrantedAuthority> authorities = roles.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

    User principal = new User(claims.getSubject(), "", authorities);

    return new UsernamePasswordAuthenticationToken(principal, null, authorities);
  }

  /**
   * 토큰에서 클레임 추출
   * @param token JWT 토큰
   * @return Claims 객체
   */
  private Claims getClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey) // 서명 키 설정
        .build()
        .parseClaimsJws(token) // 토큰 파싱
        .getBody();
  }

  /**
   * JWT 유효성 검증
   * @param token 검증할 JWT
   * @return 유효하면 true, 그렇지 않으면 false
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(secretKey) // 서명 키 설정
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false; // 유효하지 않은 토큰
    }
  }

  /**
   * 요청 헤더에서 JWT 추출
   * @param request HttpServletRequest 객체
   * @return JWT 문자열
   */
  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // "Bearer " 이후의 토큰 반환
    }
    return null;
  }
}


