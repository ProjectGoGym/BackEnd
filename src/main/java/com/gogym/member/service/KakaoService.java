package com.gogym.member.service;

import com.gogym.member.dto.KakaoLoginResponse;
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
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberService memberService;

  @Value("${kakao.rest-api-key}")
  private String kakaoClientId;

  // Redirect URI 생성 메서드
  private String generateRedirectUri() {
    // return "https://gogym-eight.vercel.app/kakaoLogin";
    return "http://localhost:8080/api/kakao/sign-in";
  }

  public KakaoLoginResponse processKakaoLogin(String code) {
    // 1. Access Token 및 프로필 정보 획득
    KakaoTokenResponse tokenResponse = requestAccessTokenFromKakao(code);
    KakaoProfileResponse profileResponse = getProfile(tokenResponse.accessToken());

    // 2. 이메일 추출
    String email = profileResponse.kakaoAccount().email();
    log.info("카카오 사용자 이메일: {}", email);

    // 3. 이메일 존재 여부 확인
    if (!memberRepository.existsByEmail(email)) {
      log.warn("회원 정보를 찾을 수 없음: {}", email);
      return new KakaoLoginResponse(false, email); // 신규 유저
    }

    // 4. Optional을 사용한 안전한 회원 정보 조회
    Optional<Member> optionalMember = memberRepository.findByEmail(email);

    if (optionalMember.isEmpty()) {
      log.warn("회원 정보를 찾을 수 없음: {}", email);
      return new KakaoLoginResponse(false, email); // 신규 유저
    }

    Member member = optionalMember.get();

    // 5. 카카오 사용자인지 확인
    if (!member.isKakao()) {
      log.warn("회원이 카카오 사용자가 아님 - 이메일: {}", email);
      return new KakaoLoginResponse(false, email); // 신규 유저
    }

    // 6. JWT 토큰 발행
    String token = jwtTokenProvider.createToken(member.getEmail(), member.getId(),
        List.of(member.getRole().name()));

    // 7. 기존 유저로 처리
    log.info("기존 유저 로그인 성공: {}", email);
    return new KakaoLoginResponse(true, email); // 기존 유저
  }



  // 카카오 인증 URL 생성 메서드
  public String getKakaoAuthUrl(String currentDomain) {
    String redirectUri = generateRedirectUri();
    return String.format(
        "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
        kakaoClientId, redirectUri);
  }

  // Access Token 요청 메서드
  private KakaoTokenResponse requestAccessTokenFromKakao(String code) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

    String redirectUri = generateRedirectUri();
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

  public String generateJwtToken(String email) {
    Optional<Member> optionalMember = memberRepository.findByEmail(email);

    if (optionalMember.isEmpty()) {
      log.warn("회원 정보를 찾을 수 없음 - 이메일: {}", email);
      return null; // 신규 유저 처리
    }

    Member member = optionalMember.get();

    return jwtTokenProvider.createToken(member.getEmail(), member.getId(),
        List.of(member.getRole().name()));
  }


}
