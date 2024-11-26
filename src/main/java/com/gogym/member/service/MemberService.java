package com.gogym.member.service;

import com.gogym.common.response.ErrorCode;
import com.gogym.config.JwtTokenProvider;
import com.gogym.member.dto.MemberDto;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;


/**
 * 사용자 계정 및 인증 관련 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;
  private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1시간 분 초 밀리초
  private final EmailService emailService;

  /**
   * 회원가입 처리
   * @param request 회원가입 요청 데이터
   */
  public void signUp(MemberDto.SignUpRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new IllegalStateException(ErrorCode.DUPLICATE_EMAIL.getMessage());
    }

    if (memberRepository.existsByNickname(request.getNickname())) {
      throw new IllegalStateException(ErrorCode.DUPLICATE_NICKNAME.getMessage());
    }

    Member member = new Member();
    member.setName(request.getName());
    member.setEmail(request.getEmail());
    member.setNickname(request.getNickname());
    member.setPhone(request.getPhone());
    member.setPassword(passwordEncoder.encode(request.getPassword()));
    member.setRole(request.getRole());
    member.setProfileImageUrl(request.getProfileImageUrl());
    member.setInterestArea1(request.getInterestArea1());
    member.setInterestArea2(request.getInterestArea2());

    memberRepository.save(member);
  }

  /**
   * 로그인 처리
   * @param request 로그인 요청 데이터
   * @return 로그인 응답 데이터 (닉네임, JWT 토큰)
   */
  public MemberDto.LoginResponse signIn(MemberDto.LoginRequest request) {
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalStateException(ErrorCode.EMAIL_NOT_FOUND.getMessage()));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new IllegalStateException(ErrorCode.INVALID_PASSWORD.getMessage());
    }

    // 사용자 권한 정보 가져오기
    List<String> roles = List.of(member.getRole());

    // JWT 토큰 생성
    String token = jwtTokenProvider.createToken(member.getEmail(), roles);

    // 응답 생성
    MemberDto.LoginResponse response = new MemberDto.LoginResponse();
    response.setNickname(member.getNickname());
    response.setToken(token);
    return response;
  }

  /**
   * 비밀번호 재설정 처리
   * @param request 비밀번호 재설정 요청 데이터
   */
  public void resetPassword(MemberDto.ResetPasswordRequest request) {
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalStateException(ErrorCode.EMAIL_NOT_FOUND.getMessage()));

    member.setPassword(passwordEncoder.encode(request.getNewPassword()));
    memberRepository.save(member);
  }

  /**
   * 이메일 중복 확인
   * @param email 확인할 이메일
   * @return 중복 여부 (true: 사용 가능, false: 중복)
   */
  public boolean checkEmail(String email) {
    return !memberRepository.existsByEmail(email);
  }

  /**
   * 닉네임 중복 확인
   * @param nickname 확인할 닉네임
   * @return 중복 여부 (true: 사용 가능, false: 중복)
   */
  public boolean checkNickname(String nickname) {
    return !memberRepository.existsByNickname(nickname);
  }

  /**
   * 로그아웃 처리
   * 현재 사용 중인 JWT 토큰을 무효화.
   *
   * @param token 클라이언트가 전달한 JWT 토큰
   */
  public void signOut(String token) {
    // Redis에 토큰을 저장하고, 유효 시간이 끝날 때까지 무효화 처리
    redisTemplate.opsForValue().set(token, "logout", TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
  }

  
  /**
   * 이메일 검증 및 인증 링크 발송
   */
  public void sendEmailVerification(String email) {
    // 이메일 형식 유효성 검사 (필요시 정규식 검증)
    if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다.");
    }

    // 회원 이메일 존재 여부 확인
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("해당 이메일로 가입된 회원이 존재하지 않습니다."));

    // 인증 토큰 생성
    String token = UUID.randomUUID().toString();
    member.setEmailVerificationToken(token);

    // 저장
    memberRepository.save(member);

    // 이메일 발송
    emailService.sendVerificationEmail(email, token);
  }
  
  /**
   * 이메일 인증 코드 확인 처리
   * @param request 이메일 인증 코드 요청 데이터
   */
  public void verifyCode(MemberDto.VerifyCodeRequest request) {
    // Redis에서 저장된 인증 코드를 가져옴
    String storedCode = redisTemplate.opsForValue().get(request.getEmail());

    // 인증 코드가 없거나 일치하지 않으면 에러 처리
    if (storedCode == null || !storedCode.equals(request.getCode())) {
      throw new IllegalStateException(ErrorCode.EMAIL_NOT_FOUND.getMessage());
    }

    // 인증 코드가 일치하면 인증 코드 삭제
    redisTemplate.delete(request.getEmail());
  }
  
  public void verifyEmailToken(String token) {
    // 토큰으로 회원 찾기
    Member member = memberRepository.findByEmailVerificationToken(token)
        .orElseThrow(() -> new IllegalStateException("유효하지 않은 인증 토큰입니다."));

    // 이메일 인증 완료 처리
    member.setEmailVerified(true);
    member.setEmailVerificationToken(null);

    memberRepository.save(member);
  }
}





