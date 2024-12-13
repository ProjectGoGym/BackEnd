package com.gogym.member.service;

import java.util.List;
import java.util.Optional;
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
import com.gogym.member.entity.KakaoMember;
import com.gogym.member.entity.Member;
import com.gogym.member.entity.Role;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.KakaoMemberRepository;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoService {

  private final MemberRepository memberRepository;
  private final KakaoMemberRepository kakaoMemberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberService memberService;

  @Value("${kakao.rest-api-key}")
  private String kakaoClientId;

  @Value("${kakao.redirect-uri}")
  private String redirectUri;

  @Transactional
  public String handleKakaoCallback(String code) {
    KakaoTokenResponse tokenResponse = getAccessToken(code);
    KakaoProfileResponse profileResponse = getProfile(tokenResponse.getAccessToken());

    Member member = findOrCreateMember(profileResponse);
    String token = jwtTokenProvider.createToken(member.getEmail(), member.getId(),
        List.of(member.getRole().name()));

    return token;
  }

  private KakaoTokenResponse getAccessToken(String code) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/x-www-form-urlencoded");

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

  @Transactional
  private Member createMember(KakaoProfileResponse profile) {
    String email = profile.getKakaoAccount().getEmail();
    String nickname = "고짐이_" + (int) (Math.random() * 10000);

    Member newMember = Member.builder().email(email).nickname(nickname).name(email.split("@")[0])
        .role(Role.USER).password("") // 비밀번호 없음
        .phone("") // 기본값
        .isKakao(true).memberStatus("APPROVED").build();

    memberRepository.save(newMember);

    KakaoMember kakaoMember = KakaoMember.builder().kakaoId(profile.getId()).member(newMember)
        .uuid(java.util.UUID.randomUUID().toString()).build();

    kakaoMemberRepository.save(kakaoMember);

    return newMember;
  }

  private Member findOrCreateMember(KakaoProfileResponse profile) {
    String email = profile.getKakaoAccount().getEmail();

    return memberRepository.findByEmail(email).orElseGet(() -> createMember(profile));
  }
}


