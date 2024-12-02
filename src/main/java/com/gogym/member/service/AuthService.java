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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1시간 (밀리초)
  private static final String EMAIL_VERIFICATION_PREFIX = "emailVerification:";

  @Value("${app.verification-url}")
  private String verificationUrlTemplate;
  
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final JavaMailSender mailSender;
  private final RedisTemplate<String, String> redisTemplate;
  
  // 회원가입 처리
  @Transactional
  public void signUp(SignUpRequest request) {
      validateEmail(request.getEmail()); // 이메일 중복 확인
      validateNickname(request.getNickname()); // 닉네임 중복 확인

      // Dto → Entity 변환
      Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));

      // 회원 데이터 저장
      memberRepository.save(member);
  }

  // 로그인 처리
  public String login(SignInRequest request) {
    // 이메일로 사용자 조회
    Member member = memberRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    // 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // JWT 토큰 생성 후 반환
    return jwtTokenProvider.createToken(member.getEmail(), List.of(member.getRole().name()));
  }


  // 비밀번호 재설정 처리
  @Transactional
  public void resetPassword(String authorizationHeader, ResetPasswordRequest request) {
    String token = extractToken(authorizationHeader);
    // JWT 토큰에서 이메일 추출
    String authenticatedEmail = jwtTokenProvider.getAuthentication(token).getName();
    
    // 요청된 이메일과 인증된 이메일 비교
    if (!authenticatedEmail.equals(request.getEmail())) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // 이메일로 사용자 찾기
    Member member = memberRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 새 비밀번호 암호화 후 저장
    member.setPassword(passwordEncoder.encode(request.getNewPassword()));
    memberRepository.save(member);
  }

  // 로그아웃 처리
  public void logout(String authorizationHeader) {
    String token = extractToken(authorizationHeader);
    redisTemplate.opsForValue().set(token, "logout", 3600000, TimeUnit.MILLISECONDS);
  }

  // 토큰 추출
  public String extractToken(String authorizationHeader) {
    return authorizationHeader.replace("Bearer ", "");
  }

  // 이메일 중복 확인
  public void validateEmail(String email) {
      if (memberRepository.existsByEmail(email)) {
          throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
      }
  }
  
  // 닉네임 중복 확인
  public void validateNickname(String nickname) {
      if (memberRepository.existsByNickname(nickname)) {
          throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
      }
  }
  
  //이메일로 사용자 조회
  public Member findMemberByEmail(String email) {
      return memberRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
  }

  // 이메일 인증 요청
  public void sendVerificationEmail(String email) {
    // 토큰 생성
    String token = UUID.randomUUID().toString();

    // Redis에 토큰 저장
    redisTemplate.opsForValue().set(EMAIL_VERIFICATION_PREFIX + token, email, TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);

    // 인증 URL 생성
    String verificationUrl = verificationUrlTemplate + token;

    // 이메일 발송
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("이메일 인증 요청");
    message.setText("다음 링크를 클릭하여 이메일을 인증하세요: " + verificationUrl);

    mailSender.send(message);
  }

  // 이메일 인증 확인
  @Transactional
  public void verifyEmailToken(String token) {
    // Redis에서 토큰 검증
    String email = redisTemplate.opsForValue().get(EMAIL_VERIFICATION_PREFIX + token);

    if (email == null) {
      throw new CustomException(ErrorCode.INVALID_TOKEN); // 토큰이 유효하지 않음
    }

    // 이메일 인증 상태 업데이트
    Member member = memberRepository.findByEmail(email)
      .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    member.verifyEmail();
    memberRepository.save(member);

    // 사용한 토큰 삭제
    redisTemplate.delete(EMAIL_VERIFICATION_PREFIX + token);
  }
  
  // 사용자 조회하는 메서드, [맴버 객체]를 반환
  public Member getById(String token) {
      String email;
      
      // JWT 토큰에서 이메일 추출
      try {
          email = jwtTokenProvider.getAuthentication(token).getName();
      } catch (Exception e) {
          throw new CustomException(ErrorCode.INVALID_TOKEN);
      }

      // 사용자 조회 -> 반환
      return memberRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
  }
}



