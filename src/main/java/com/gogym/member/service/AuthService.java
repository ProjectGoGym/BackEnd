package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.LoginResponse;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // Dto에서 Entity 변환
    Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));
    // 회원 데이터 저장
    memberRepository.save(member);
  }

  // 로그인 처리
  public String login(SignInRequest request) {
    // 사용자의 이메일로 회원 정보 조회
    Member member = memberService.findByEmail(request.getEmail());

    // 이메일 인증 여부 확인
    if (!member.isVerified()) {
      throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    // 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // JWT 토큰 생성 (토큰 생성 부분)
    String token = jwtTokenProvider.createToken(
        member.getEmail(),
        member.getId(),
        List.of(member.getRole().name()) // 사용자의 역할을 기반으로 토큰 생성
    );

    // JWT 토큰을 Redis에 저장 (로그인 상태 관리)
    redisTemplate.opsForValue().set("login:" + member.getId(), token, 60 * 60 * 24, TimeUnit.SECONDS); // 24시간 동안 유효

    // 생성된 JWT 토큰 반환
    return token;
    
  }
  
  // 비밀번호 재설정 처리
  @Transactional
  public void resetPassword(String email, ResetPasswordRequest request) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    // 새 비밀번호 암호화 후 저장
    member.setPassword(passwordEncoder.encode(request.getNewPassword()));
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
  public void logout(Long memberId) {
    redisTemplate.delete("login:" + memberId);//TODO - redis 비교-저장해서 사용하는 부분 꼭 필요한지 
  }

  // 닉네임 중복 확인
  public void validateNickname(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }
    
  }

  //JWT 토큰에서 인증된 이메일 추출
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

  //사용자 조회하는 메서드, [맴버 객체]를 반환
  public Member getById(HttpServletRequest request) {
    // JWT 토큰에서 인증된 이메일 추출, HttpServletRequest 객체 전달
    String email = extractAuthenticatedEmail(request);
   
    // 이메일로 사용자 조회 -> 반환
   return memberService.findByEmail(email);
   
  }

  
  //이메일로 회원 정보 조회
  public Member getMemberByEmail(String email) {
    return memberService.findByEmail(email);
    
  }
  
}