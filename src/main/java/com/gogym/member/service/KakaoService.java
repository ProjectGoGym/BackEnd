package com.gogym.member.service;

import com.gogym.member.dto.KakaoProfileResponse;
import com.gogym.member.dto.KakaoTokenResponse;
import com.gogym.member.entity.Member;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoService {

  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private static final Logger log = LoggerFactory.getLogger(KakaoService.class);

  @Value("${kakao.rest-api-key}")
  private String kakaoClientId;

  // Redirect URI 생성 메서드
  private String generateRedirectUri(String currentDomain) {
    return currentDomain + "/api/kakao/sign-in";
  }

  @Transactional
  public String processKakaoLogin(String code, String currentDomain) {
    log.info("카카오 로그인 처리 시작 - 인증 코드: {}, 도메인: {}", code, currentDomain);

    // 1. Access Token 및 프로필 정보 획득
    KakaoTokenResponse tokenResponse = getAccessToken(code, currentDomain);
    KakaoProfileResponse profileResponse = getProfile(tokenResponse.accessToken());

    // 2. 이메일로 회원 정보 조회
    String email = profileResponse.kakaoAccount().email();
    log.info("카카오 사용자 이메일: {}", email);

    Optional<Member> optionalMember = memberRepository.findByEmail(email);

    if (optionalMember.isEmpty()) {
      log.warn("회원 정보 없음 - 이메일: {}", email);
      return null; // 회원 정보가 없는 경우는 클라이언트에 false 반환
    }

    Member member = optionalMember.get();

    // 3. isKakao 여부 확인
    if (!member.isKakao()) {
      log.warn("회원이 카카오 사용자가 아님 - 이메일: {}", email);
      return null; // 일반 회원가입 진행 필요
    }

    // 4. 로그인 진행: JWT 토큰 발행
    String token = jwtTokenProvider.createToken(member.getEmail(), member.getId(),
        List.of(member.getRole().name()));
    log.info("JWT 토큰 생성 완료 - 이메일: {}", email);
    return token;
  }

  // 카카오 인증 URL 생성 메서드
  public String getKakaoAuthUrl(String currentDomain) {
    String redirectUri = generateRedirectUri(currentDomain);
    return String.format(
        "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
        kakaoClientId, redirectUri);
  }

  // Access Token 요청 메서드
  private KakaoTokenResponse getAccessToken(String code, String currentDomain) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

    String redirectUri = generateRedirectUri(currentDomain);
    String body =
        String.format("grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
            kakaoClientId, redirectUri, code);

    log.info("카카오 액세스 토큰 요청 - 인증 코드: {}", code);
    log.info("카카오 액세스 토큰 요청 - Redirect URI: {}", redirectUri);

    HttpEntity<String> request = new HttpEntity<>(body, headers);
    ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
        "https://kauth.kakao.com/oauth/token", HttpMethod.POST, request, KakaoTokenResponse.class);

    log.info("카카오 액세스 토큰 응답: {}", response.getBody());
    return response.getBody();
  }


  private KakaoProfileResponse getProfile(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    log.info("카카오 사용자 정보 요청 - 액세스 토큰: {}", accessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<KakaoProfileResponse> response = restTemplate.exchange(
        "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, KakaoProfileResponse.class);

    log.info("카카오 사용자 정보 응답: {}", response.getBody());
    return response.getBody();
  }

}
