package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.security.core.Authentication;
import com.gogym.member.dto.LoginResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  // TODO : 서버 주소 또는 도메인 파면 변경해야할 부분
  private final MemberService memberService;
  
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;
  
  //회원가입 처리
  @Transactional
  public void signUp(SignUpRequest request) {
    // Dto → Entity 변환
    Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));
    // 회원 데이터 저장
    memberRepository.save(member);
  }

  // 로그인 처리
  public LoginResponse login(SignInRequest request) {
  // 이메일로 사용자 조회
  Member member = memberService.findByEmail(request.getEmail());

  // 비밀번호 검증
  if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
    throw new CustomException(ErrorCode.UNAUTHORIZED);
  }


  //JWT 토큰 생성
  String token = jwtTokenProvider.createToken(
      member.getEmail(),
      member.getId(), // memberId 추가
      List.of(member.getRole().name())
  );
  
  // 사용자 정보 반환
  return new LoginResponse(
      member.getEmail(),
      member.getName(),
      member.getNickname(),
      member.getPhone()
  );
  
  }
  
  // 비밀번호 재설정 처리
  @Transactional
  public void resetPassword(Long memberId, ResetPasswordRequest request) {
    Member member = memberService.findById(memberId);
    validateAuthenticatedEmail(member.getEmail(), request.getEmail());

    // 비밀번호 업데이트
    updatePassword(request.getEmail(), request.getNewPassword());
    
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
  public void logout(Long memberId) {
    redisTemplate.opsForValue().set("logout:" + memberId, "logout", 600000, TimeUnit.MILLISECONDS);
  }

  // 닉네임 중복 확인
  public void validateNickname(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }
    
  }

  // JWT 토큰에서 인증된 이메일 추출
  private String extractAuthenticatedEmail(String authorizationHeader) {
    String token = jwtTokenProvider.extractToken(null, authorizationHeader);
    Authentication authentication = jwtTokenProvider.getAuthentication(token);
    if (authentication == null || authentication.getName() == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
      
    }
    
    return authentication.getName();
    
  }

  // 사용자 조회하는 메서드, [맴버 객체]를 반환
  public Member getById(String authorizationHeader) {
    // JWT 토큰에서 인증된 이메일 추출
    String email = extractAuthenticatedEmail(authorizationHeader);
    
    // 이메일로 사용자 조회 -> 반환
    return memberService.findByEmail(email);
    
  }
  
}