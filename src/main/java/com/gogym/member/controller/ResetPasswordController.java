package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.service.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {
  //비밀번호 재설정 비즈니스 로직을 처리하는 서비스
  private final ResetPasswordService resetPasswordService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<Void>> resetPassword(
    @RequestBody @Valid ResetPasswordRequest request // 요청 데이터 검증 및 전달
  ) {
    resetPasswordService.resetPassword(request); // 비밀번호 재설정 처리
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.PASSWORD_RESET_SUCCESS)); // 성공 응답 반환
  }
}
