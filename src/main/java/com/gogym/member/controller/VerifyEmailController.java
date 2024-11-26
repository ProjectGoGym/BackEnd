package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.exception.CustomException;
import com.gogym.member.service.VerifyEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/verify-email")
@RequiredArgsConstructor
public class VerifyEmailController {

  // 이메일 인증 토큰 검증을 처리하는 서비스
  private final VerifyEmailService verifyEmailService;

  @GetMapping
  public ResponseEntity<ApplicationResponse<Void>> verifyEmail(
    @RequestParam String token // 요청 파라미터로 인증 토큰 전달
  ) {
    try {
      verifyEmailService.verifyEmailToken(token); // 이메일 인증 토큰 검증 처리
      return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.EMAIL_VERIFICATION_SUCCESS)); // 성공 응답 반환
    } catch (CustomException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 오류 상태 반환
        .body(ApplicationResponse.error(e.getErrorCode())); // 에러 응답 반환
    }
  }
}

