package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.dto.LoginRequest;
import com.gogym.member.dto.LoginResponse;
import com.gogym.member.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/login")
@RequiredArgsConstructor
public class LoginController {
//로그인 처리 비즈니스 로직을 담당하는 서비스
  private final LoginService loginService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<LoginResponse>> login(
    @RequestBody @Valid LoginRequest request // 로그인 요청 데이터를 검증 및 전달
  ) {
    LoginResponse response = loginService.login(request); // 로그인 서비스 호출
    return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.LOGIN_SUCCESS)); // 성공 응답 반환
  }
}
