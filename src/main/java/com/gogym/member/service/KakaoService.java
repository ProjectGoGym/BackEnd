package com.gogym.member.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.gogym.member.dto.KakaoProfileResponse;
import com.gogym.member.dto.KakaoTokenResponse;
import com.gogym.member.entity.Member;
import com.gogym.member.entity.Role;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoService {

  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${kakao.rest-api-key}")
  private String kakaoClientId;

  // Redirect URI 생성 메서드
  private String generateRedirectUri(String currentDomain) {
    return currentDomain + "/api/kakao/sign-in";
  }

  @Transactional
  public String processKakaoLogin(String code, String currentDomain) {
    KakaoTokenResponse tokenResponse = getAccessToken(code, currentDomain);
    KakaoProfileResponse profileResponse = getProfile(tokenResponse.accessToken());

    Member member = findOrCreateMember(profileResponse);
    return jwtTokenProvider.createToken(member.getEmail(), member.getId(),
        List.of(member.getRole().name()));
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
    headers.set("Content-Type", "application/x-www-form-urlencoded");

    String redirectUri = generateRedirectUri(currentDomain);
    String body =
        String.format("grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s",
            kakaoClientId, redirectUri, code);

    HttpEntity<String> request = new HttpEntity<>(body, headers);
    ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
        "https://kauth.kakao.com/oauth/token", HttpMethod.POST, request, KakaoTokenResponse.class);
    return response.getBody();
  }


  private KakaoProfileResponse getProfile(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);
    ResponseEntity<KakaoProfileResponse> response = restTemplate.exchange(
        "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, KakaoProfileResponse.class);
    return response.getBody();
  }

  private String generateUniqueNickname() {
    // 수식어 리스트
    String[] add = {"화려한", "건강한", "상냥한", "착한", "활기찬", "강력한", "멋쟁이"};
    // 랜덤 수식어
    String randomAdjective = add[(int) (Math.random() * add.length)];
    // 고유한 6자리 닉네임 생성
    String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

    // 닉네임 조합
    return randomAdjective + " 고짐이_" + uniqueId;
  }


  @Transactional
  private Member createMember(KakaoProfileResponse profile) {
    String email = profile.kakaoAccount().email();
    String nickname = generateUniqueNickname();

    Member newMember = Member.builder().email(email).nickname(nickname).name(email.split("@")[0])
        .role(Role.USER).password("").phone("").isKakao(true).memberStatus("APPROVED").build();

    memberRepository.save(newMember);

    return newMember;
  }

  private Member findOrCreateMember(KakaoProfileResponse profile) {
    String email = profile.kakaoAccount().email();
    return memberRepository.findByEmail(email).orElseGet(() -> createMember(profile));
  }
}

