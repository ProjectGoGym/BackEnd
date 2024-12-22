package com.gogym.member.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.BanNicknameRepository;
import com.gogym.member.repository.MemberRepository;
import com.gogym.util.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final MemberService memberService;

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final EmailService emailService;
  private final BanNicknameRepository banNicknameRepository;
  private final RedisService redisService;

  // 회원가입 처리
  @Transactional
  public void signUp(SignUpRequest request, boolean isKakao) {
    // 이메일 중복 확인
    emailService.validateEmail(request.getEmail());

    // Dto에서 Entity 변환
    Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));

    // 회원 데이터 저장
    memberRepository.save(member);

    // isKakao가 true인 경우 추가 처리
    if (isKakao) {
      completeKakaoSignUp(request.getEmail());
    }
  }

  // 로그인 처리
  public Map<String, String> login(SignInRequest request) {
    // 사용자 인증 로직
    Member member = memberService.findByEmail(request.getEmail());
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // Access Token 및 Refresh Token 생성
    String accessToken = jwtTokenProvider.createToken(member.getEmail(), member.getId(),
        List.of(member.getRole().name()));
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getId());

    // Redis에 Refresh Token 저장
    String redisKey = "refresh:" + member.getId();
    redisService.save(redisKey, refreshToken, jwtTokenProvider.getRefreshTokenValidityInSeconds());

    // 토큰 맵 생성
    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);

    return tokens;
  }

  // 비밀번호 재설정 처리
  @Transactional
  public void resetPassword(HttpServletRequest request, ResetPasswordRequest resetRequest) {
    // JWT 토큰 추출 및 인증
    String token = jwtTokenProvider.extractToken(request);
    if (token == null || token.isEmpty()) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    Authentication authentication = jwtTokenProvider.getAuthentication(token);
    if (authentication == null || authentication.getName() == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    String authenticatedEmail = authentication.getName();
    if (!authenticatedEmail.equals(resetRequest.getEmail())) {
      throw new CustomException(ErrorCode.FORBIDDEN);
    }

    // 이메일로 사용자 조회 및 기존 비밀번호 확인
    Member member = memberService.findByEmail(resetRequest.getEmail());
    validateCurrentPassword(resetRequest.getCurrentPassword(), member.getPassword());

    // 비밀번호 재설정
    member.setPassword(passwordEncoder.encode(resetRequest.getNewPassword()));
    memberRepository.save(member);
  }

  // 인증된 이메일과 요청된 이메일 비교
  private void validateAuthenticatedEmail(String authenticatedEmail, String requestedEmail) {
    if (!authenticatedEmail.equals(requestedEmail)) {
      throw new CustomException(ErrorCode.FORBIDDEN);
    }
  }

  // 비밀번호 업데이트
  private void updatePassword(String email, String newPassword) {
    Member member = memberService.findByEmail(email);
    member.setPassword(passwordEncoder.encode(newPassword));
  }

  // 로그아웃 처리
  public void logout(HttpServletRequest request) {
    String token = jwtTokenProvider.extractToken(request);

    if (token == null || token.isEmpty()) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    if (!jwtTokenProvider.validateToken(token)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

  }

  // 닉네임 중복 확인
  public void validateNickname(String nickname) {
    if (memberRepository.existsByNickname(nickname)
        || banNicknameRepository.existsByBannedNickname(nickname)) {
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

  }

  // JWT 토큰에서 인증된 이메일 추출
  public String extractAuthenticatedEmail(HttpServletRequest request) {
    // request에서 Authorization 헤더를 사용하여 토큰 추출

    String token = jwtTokenProvider.extractToken(request);

    // 토큰을 이용하여 인증 정보 가져오기
    Authentication authentication = jwtTokenProvider.getAuthentication(token);


    // 인증 정보가 없거나 인증된 이메일이 없는 경우 예외 처리
    if (authentication == null || authentication.getName() == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // 인증된 이메일 반환
    return authentication.getName();

  }

  // 사용자 조회하는 메서드, [맴버 객체]를 반환
  public Member getById(HttpServletRequest request) {
    // JWT 토큰에서 인증된 이메일 추출, HttpServletRequest 객체 전달
    String email = extractAuthenticatedEmail(request);

    // 이메일로 사용자 조회 -> 반환
    return memberService.findByEmail(email);

  }


  // 이메일로 회원 정보 조회
  public Member getMemberByEmail(String email) {
    return memberService.findByEmail(email);

  }

  // 현재 비밀번호를 검증
  private void validateCurrentPassword(String inputPassword, String storedPassword) {
    if (!passwordEncoder.matches(inputPassword, storedPassword)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED); // 기존 비밀번호가 틀린 경우
    }
  }

  // 카카오 회원가입 업데이트 로직
  @Transactional
  public void completeKakaoSignUp(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // isKakao 값을 true로 업데이트
    member.setKakao(true);
  }

  // 토큰 갱신
  @Transactional
  public String refreshAccessToken(String refreshToken) {
    // Refresh Token 유효성 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // Redis에서 Refresh Token 조회
    Long memberId = jwtTokenProvider.extractMemberId(refreshToken);
    String redisKey = "refresh:" + memberId;
    String storedToken = redisService.get(redisKey);

    if (storedToken == null || !storedToken.equals(refreshToken)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // 새로운 Access Token 생성
    String email = jwtTokenProvider.getClaims(refreshToken).getSubject();
    return jwtTokenProvider.createToken(email, memberId, List.of("ROLE_USER"));
  }

}
