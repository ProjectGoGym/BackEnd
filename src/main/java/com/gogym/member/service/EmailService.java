package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final long TOKEN_EXPIRATION_TIME = 30 * 60 * 1000; // 30분
  private static final String EMAIL_VERIFICATION_PREFIX = "emailVerification:";
  
  //변경해야할 부분
  //private static final String SERVER_URL = "http://3.36.198.162:8080/";
  private static final String SERVER_URL = "http://localhost:8080/";
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

    // 이메일 발송 (HTML 사용해서)
    try {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(email);
        helper.setSubject("GOGYM 회원가입 이메일 인증 요청입니다.");

        // HTML 본문 작성
        String htmlContent = "<html>" +
            "<body>" +
            "<p style='margin-bottom: 20px;'>다음 링크를 클릭하여 이메일을 인증하세요:</p>" +
            "<a href='" + verificationUrl + "' style='padding: 10px 15px; color: white; " + 
            "background-color: skyblue; text-decoration: none; border-radius: 5px; display: inline-block;'>이메일 인증하기</a>" +
            "</body>" +
            "</html>";

        // HTML 포맷 설정
        helper.setText(htmlContent, true);

        // 이메일 전송
        mailSender.send(mimeMessage);
    } catch (MessagingException e) {
        throw new RuntimeException("이메일 전송에 실패했습니다.", e);
    }
}

  // 이메일 인증 확인
  @Transactional
  public void verifyEmailToken(String token) {
      // Redis에서 토큰 검색
      String email = redisTemplate.opsForValue().get(EMAIL_VERIFICATION_PREFIX + token);

      if (email == null) {
          throw new CustomException(ErrorCode.INVALID_TOKEN);
      }

      // 회원 정보 업데이트
      Member member = memberService.findByEmail(email);

      if (member == null) {
          throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
      }

      member.setVerifiedAt(LocalDateTime.now());
      memberRepository.save(member);

      // 인증 완료 후 토큰 삭제
      boolean isDeleted = redisTemplate.delete(EMAIL_VERIFICATION_PREFIX + token);
  }
}

