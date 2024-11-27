package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.service.CheckEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/check-email")
@RequiredArgsConstructor
public class CheckEmailController {

  private final CheckEmailService checkEmailService;

  @GetMapping
  public ResponseEntity<ApplicationResponse<Boolean>> checkEmail(
    @RequestParam("email") String email
  ) {// 이메일 중복 확인 서비스 호출
    boolean isAvailable = checkEmailService.checkEmail(email);
    
    // 중복 여부에 따라 적절한 성공 코드를 포함한 응답 생성
    return ResponseEntity.ok(
      ApplicationResponse.ok(
        isAvailable, // true: 사용 가능, false: 중복
        isAvailable ? SuccessCode.EMAIL_AVAILABLE : SuccessCode.DUPLICATE_EMAIL
      )
    );
  }
}
