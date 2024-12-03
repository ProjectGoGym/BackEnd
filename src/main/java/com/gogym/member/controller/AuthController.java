package com.gogym.member.controller;

import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gogym.member.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  
  private final AuthService authService;

  // 회원가입
  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(
      @RequestBody @Valid SignUpRequest request
  ) {
    authService.signUp(request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  // 로그인
  @PostMapping("/sign-in")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid SignInRequest request) {
    LoginResponse loginResponse = authService.login(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getToken())
        .body(loginResponse);  
  }

  // 로그아웃
  @PostMapping("/sign-out")
  public ResponseEntity<Void> logout(
      @RequestHeader("Authorization") String authorizationHeader
  ) {
    authService.logout(authorizationHeader);
    return ResponseEntity.noContent().build();
  }

  // 비밀번호 재설정
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(
      @RequestHeader("Authorization") String authorizationHeader,
      @RequestBody @Valid ResetPasswordRequest request
  ) {
    authService.resetPassword(authorizationHeader, request);
    return ResponseEntity.noContent().build();
  }

  // 이메일 중복 확인
  @GetMapping("/check-email")
  public ResponseEntity<Void> checkEmail(@RequestParam("email") String email) {
    authService.validateEmail(email);
    return ResponseEntity.ok().build();
  }

  // 닉네임 중복 확인
  @GetMapping("/check-nickname")
  public ResponseEntity<Void> checkNickname(@RequestParam("nickname") String nickname) {
    authService.validateNickname(nickname);
    return ResponseEntity.ok().build();
  }

  // 이메일 인증 확인
  @GetMapping("/verify-email")
  public ResponseEntity<Void> verifyEmail(@RequestParam(name = "token") String token) {
    authService.verifyEmailToken(token);
    return ResponseEntity.ok().build();
  }

  // 이메일 인증 요청
  @PostMapping(value = "/send-verification-email", produces = "application/json")
  public ResponseEntity<Void> sendVerificationEmail(
      @RequestParam(name = "email") String email
  ) {
    authService.sendVerificationEmail(email);
    return ResponseEntity.noContent().build();
  }
}



