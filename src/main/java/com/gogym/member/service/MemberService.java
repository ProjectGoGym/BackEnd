package com.gogym.member.service;

import com.gogym.common.response.ErrorCode;
import com.gogym.config.JwtTokenProvider;
import com.gogym.exception.CustomException;
import com.gogym.member.dto.MemberDto;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//사용자 계정 및 인증 관련 서비스 클래스
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;
  private final EmailService emailService;
  private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1시간 (밀리초)

  //회원가입 처리
  public MemberDto.SignUpResponse signUp(MemberDto.SignUpRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }

    if (memberRepository.existsByNickname(request.getNickname())) {
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    // Member 객체 생성 및 저장
    Member member = Member.builder()
        .name(request.getName())
        .email(request.getEmail())
        .nickname(request.getNickname())
        .phone(request.getPhone())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .profileImageUrl(request.getProfileImageUrl())
        .interestArea1(request.getInterestArea1())
        .interestArea2(request.getInterestArea2())
        .build();

    memberRepository.save(member);

    // 회원가입 완료 응답 생성
    return new MemberDto.SignUpResponse(
        member.getId(),
        member.getNickname(),
        member.getEmail()
    );
  }

  //로그인 처리
  public MemberDto.LoginResponse signIn(MemberDto.LoginRequest request) {
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_PASSWORD);
    }

    // 사용자 권한 정보 가져오기
    List<String> roles = List.of(member.getRole());

    // JWT 토큰 생성
    String token = jwtTokenProvider.createToken(member.getEmail(), roles);

    // 로그인 응답 생성
    return new MemberDto.LoginResponse(
        member.getId(),
        member.getNickname(),
        member.getEmail(),
        token,
        member.getRole()
    );
  }

  //로그아웃 처리
  public void signOut(String token) {
    // Redis에 토큰을 저장하고, 유효 시간이 끝날 때까지 무효화 처리
    redisTemplate.opsForValue().set(token, "logout", TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
  }

  //비밀번호 재설정
  public void resetPassword(MemberDto.ResetPasswordRequest request) {
    // 비밀번호 확인
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new CustomException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
    }

    // 이메일로 사용자 조회
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    // 비밀번호 암호화 후 저장
    member.setPassword(passwordEncoder.encode(request.getNewPassword()));
    memberRepository.save(member);
  }
  
  //이메일 검증 및 발송
  public void sendEmailVerification(String email) {
    // 이메일 형식 유효성 검사
    if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
      throw new CustomException(ErrorCode.INVALID_EMAIL_FORMAT);
    }

    // 회원 이메일 존재 여부 확인
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    // 인증 토큰 생성
    String token = UUID.randomUUID().toString();
    member.setEmailVerificationToken(token);

    // 저장
    memberRepository.save(member);

    // 이메일 발송
    emailService.sendVerificationEmail(email, token);
  }

  //이메일 인증 토큰 확인
  public void verifyEmailToken(String token) {
    // 토큰으로 회원 찾기
    Member member = memberRepository.findByEmailVerificationToken(token)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

    // 이메일 인증 완료 처리
    member.setEmailVerified(true);
    member.setEmailVerificationToken(null);

    memberRepository.save(member);
  }

  //이메일 중복 확인
  public boolean checkEmail(String email) {
    return !memberRepository.existsByEmail(email);
  }

  //닉네임 중복 확인
  public boolean checkNickname(String nickname) {
    return !memberRepository.existsByNickname(nickname);
  }
}