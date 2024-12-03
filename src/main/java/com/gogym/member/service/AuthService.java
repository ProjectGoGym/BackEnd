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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.security.core.Authentication;
import com.gogym.member.dto.LoginResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1시간 (밀리초)
  private static final String EMAIL_VERIFICATION_PREFIX = "emailVerification:";
  
  // TODO : 서버 주소 또는 도메인 파면 변경해야할 부분
  private static final String SERVER_URL = "http://3.36.198.162:8080/";
  private final MemberService memberService;
  
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
  public LoginResponse login(SignInRequest request) {
    // 이메일로 사용자 조회
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    // 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    // JWT 토큰 생성
    String token = jwtTokenProvider.createToken(member.getEmail(), List.of(member.getRole().name()));

    // 사용자 정보 반환 (role 제외)
    return new LoginResponse(
        member.getEmail(),
        member.getName(),
        member.getNickname(),
        member.getPhone(),
        token
    );
    
  }

  // 비밀번호 재설정 처리
  @Transactional
  public void resetPassword(String authorizationHeader, ResetPasswordRequest request) {
      //토큰에서 인증된 이메일 추출
      String authenticatedEmail = extractAuthenticatedEmail(authorizationHeader);

      //인증된 이메일과 요청된 이메일 비교
      validateAuthenticatedEmail(authenticatedEmail, request.getEmail());

      //비밀번호 업데이트
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
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    member.setPassword(passwordEncoder.encode(newPassword));
  }

  // 로그아웃 처리
  public void logout(String authorizationHeader) {
    String token = extractToken(authorizationHeader);
    redisTemplate.opsForValue().set(token, "logout", 600000, TimeUnit.MILLISECONDS);
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
    redisTemplate.opsForValue().set(EMAIL_VERIFICATION_PREFIX + token, email,
        TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);

    // 인증 URL 생성
    String verificationUrl = SERVER_URL + "api/auth/verify-email?token=" + token;

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
  
  
  // --아래 코드는 util로 따로 빼야하는지 고민중입니다--
  // jwt 토큰 추출
  public String extractToken(String authorizationHeader) {
    return authorizationHeader.replace("Bearer ", "");
    
  }
  
  // JWT 토큰에서 인증된 이메일 추출
  private String extractAuthenticatedEmail(String authorizationHeader) {
      String token = extractToken(authorizationHeader);
      Authentication authentication = jwtTokenProvider.getAuthentication(token);
      if (authentication == null || authentication.getName() == null) {
          throw new CustomException(ErrorCode.UNAUTHORIZED);
      }
      return authentication.getName();
  }
  
  // 사용자 조회하는 메서드, [맴버 객체]를 반환
  public Member getById(String authorizationHeader) {
    // 1. JWT 토큰에서 인증된 이메일 추출
    String email = extractAuthenticatedEmail(authorizationHeader);

    // 2. 이메일로 사용자 조회 -> 반환
    return memberService.findByEmail(email);
  }

}