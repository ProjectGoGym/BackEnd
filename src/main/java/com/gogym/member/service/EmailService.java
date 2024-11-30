package com.gogym.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final String EMAIL_VERIFICATION_PREFIX = "emailVerification:";
  private static final long TOKEN_EXPIRATION_TIME = 10 * 60 * 1000; // 10분 (밀리초)

  private final JavaMailSender mailSender;
  private final RedisTemplate<String, String> redisTemplate;

  // 이메일 인증 요청
  @Transactional
  public void sendVerificationEmail(String email) {
    // 토큰 생성
    String token = UUID.randomUUID().toString();

    // Redis에 토큰 저장
    redisTemplate.opsForValue().set(EMAIL_VERIFICATION_PREFIX + token, email, TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);

    // 인증 URL 생성
    String verificationUrl = "http://localhost:8080/api/members/verify-email?token=" + token;

    // 이메일 발송
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("이메일 인증 요청");
    message.setText("다음 링크를 클릭하여 이메일을 인증하세요: " + verificationUrl);

    mailSender.send(message);
  }
}



