package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.dto.SignUpResponse;
import com.gogym.member.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/sign-up")
@RequiredArgsConstructor
public class SignUpController {

  // 회원가입 비즈니스 로직을 처리하는 서비스
  private final SignUpService signUpService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<SignUpResponse>> signUp(
    @RequestBody @Valid SignUpRequest request // 요청 데이터 검증 및 전달
  ) {
    SignUpResponse response = signUpService.signUp(request); // 회원가입 처리
    return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.SIGN_UP_SUCCESS)); // 성공 응답 반환
  }
}
