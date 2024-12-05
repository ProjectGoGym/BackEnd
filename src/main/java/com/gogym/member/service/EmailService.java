package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final long TOKEN_EXPIRATION_TIME = 60 * 1000; // 10분
  private static final String EMAIL_VERIFICATION_PREFIX = "emailVerification:";
  
  //변경해야할 부분
  private static final String SERVER_URL = "http://3.36.198.162:8080/";
  private final MemberService memberService;
  private final MemberRepository memberRepository;
  private final JavaMailSender mailSender;
  private final RedisTemplate<String, String> redisTemplate;
  
  // 이메일 중복 확인
  public void validateEmail(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }
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
    Member member = memberService.findByEmail(email);
    member.verifyEmail();

    // 사용한 토큰 삭제
    redisTemplate.delete(EMAIL_VERIFICATION_PREFIX + token);
  }
}

