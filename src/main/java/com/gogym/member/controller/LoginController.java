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

  private final LoginService loginService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<LoginResponse>> login(
    @RequestBody @Valid LoginRequest request
  ) {
    LoginResponse response = loginService.login(request);
    return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.LOGIN_SUCCESS));
  }
}
