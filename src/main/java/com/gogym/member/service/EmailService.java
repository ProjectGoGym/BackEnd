package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1시간
  private static final String EMAIL_VERIFICATION_PREFIX = "emailVerification:";
  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  // 변경해야할 부분
  // private static final String SERVER_URL = "http://3.36.198.162:8080/";
  private static final String SERVER_URL = "http://localhost:8080/";
  private final MemberService memberService;
  private final MemberRepository memberRepository;
  private final JavaMailSender mailSender;
  private final RedisUtil redisUtil;

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
    redisUtil.save(EMAIL_VERIFICATION_PREFIX + token, email, TOKEN_EXPIRATION_TIME);

    logger.info("Redis에 저장된 토큰: {}, 이메일: {}", token, email);

    // 인증 URL 생성
    String verificationUrl = SERVER_URL + "api/auth/verify-email?token=" + token;

    // 이메일 발송 (HTML 사용해서)
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

      helper.setTo(email);
      helper.setSubject("GOGYM 회원가입 이메일 인증 요청입니다.");

      // HTML 본문 작성
      String htmlContent =
          "<html>" + "<body>" + "<p style='margin-bottom: 20px;'>다음 링크를 클릭하여 이메일을 인증하세요:</p>"
              + "<a href='" + verificationUrl + "' style='padding: 10px 15px; color: white; "
              + "background-color: skyblue; text-decoration: none; border-radius: 5px; "
              + "display: inline-block;'> GoGym 이용하러 가기 </a>" + "</body>" + "</html>";
      // 인증 성공시 https://gogym-eight.vercel.app 로 이동 됩니다. AuthService 이메일인증 메서드 참조해주세요
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
    String email = redisUtil.get(EMAIL_VERIFICATION_PREFIX + token);

    if (email == null) {
      logger.error("Redis에서 토큰 조회 실패: {}", token);
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    logger.info("Redis에서 조회된 이메일: {}", email);

    Member member = memberService.findByEmail(email);

    if (member == null) {
      logger.error("회원 정보 조회 실패. 이메일: {}", email);
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    logger.info("조회된 회원 정보: {}", member);

    member.setVerifiedAt(LocalDateTime.now());

    redisUtil.delete(EMAIL_VERIFICATION_PREFIX + token);
  }
}
