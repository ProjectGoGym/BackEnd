package com.gogym.member.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
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

  @Value("${kakao.rest-api-key}")
  private String kakaoClientId;

  @Value("${kakao.redirect-uri}")
  private String redirectUri;

  @Transactional
  public String handleKakaoCallback(String code) {
    KakaoTokenResponse tokenResponse = getAccessToken(code);
    KakaoProfileResponse profileResponse = getProfile(tokenResponse.getAccessToken());
    Member member = findOrCreateMember(profileResponse, tokenResponse);
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
  private KakaoMember registerKakaoMember(KakaoProfileResponse profileResponse) {
    Member newMember = Member.builder().email(profileResponse.getKakaoAccount().getEmail())
        .name(profileResponse.getKakaoAccount().getEmail().split("@")[0])
        .nickname("고짐이_" + (int) (Math.random() * 10000)).isKakao(true).memberStatus("APPROVED")
        .build();

    memberRepository.save(newMember);

    KakaoMember kakaoMember = KakaoMember.builder().kakaoId(profileResponse.getId())
        .member(newMember).uuid(java.util.UUID.randomUUID().toString()).build();

    return kakaoMemberRepository.save(kakaoMember);
  }

  @Transactional
  private Member findOrCreateMember(KakaoProfileResponse profile,
      KakaoTokenResponse tokenResponse) {
    if (profile.getKakaoAccount() == null
        || !Boolean.TRUE.equals(profile.getKakaoAccount().getHasEmail())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    if (profile.getKakaoAccount().getEmail() == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // 회원 생성 또는 조회
    return memberRepository.findByEmail(profile.getKakaoAccount().getEmail()).orElseGet(() -> {
      Member newMember = Member.builder().email(profile.getKakaoAccount().getEmail())
          .nickname("고짐이_" + (int) (Math.random() * 100000))
          .name(profile.getKakaoAccount().getEmail().split("@")[0]).role(Role.USER).password("")
          .phone("").isKakao(true).memberStatus("APPROVED").build();

      memberRepository.save(newMember);

      KakaoMember kakaoMember = KakaoMember.builder().kakaoId(profile.getId()).member(newMember)
          .uuid(java.util.UUID.randomUUID().toString()).build();

      kakaoMemberRepository.save(kakaoMember);

      return newMember;
    });
  }
}


