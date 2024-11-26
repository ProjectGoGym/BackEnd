package com.gogym.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  /**
   * 이메일 인증 링크 발송
   * @param email 수신자 이메일
   * @param token 인증 토큰
   */
  public void sendVerificationEmail(String email, String token) {
    String verificationUrl = "http://localhost:8080/api/members/verify-email?token=" + token;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("이메일 인증 요청");
    message.setText("다음 링크를 클릭하여 이메일을 인증하세요: " + verificationUrl);

    mailSender.send(message);
  }
}
