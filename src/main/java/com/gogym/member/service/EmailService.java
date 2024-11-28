package com.gogym.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.gogym.member.entity.EmailVerificationToken;
import com.gogym.member.repository.EmailVerificationTokenRepository;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final EmailVerificationTokenRepository tokenRepository;

  public void sendVerificationEmail(String email) {
    // 토큰 생성 및 저장
    EmailVerificationToken token = new EmailVerificationToken(email);
    tokenRepository.save(token);

    // 인증 URL 생성
    String verificationUrl = "http://localhost:8080/api/members/verify-email?token=" + token.getToken();

    // 이메일 발송
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("이메일 인증 요청");
    message.setText("다음 링크를 클릭하여 이메일을 인증하세요: " + verificationUrl);

    mailSender.send(message);
  }
}

