package com.gogym.member.controller;

import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.common.annotation.LoginMemberId;
import com.gogym.member.dto.LoginResponse;
import com.gogym.member.service.AuthService;
import com.gogym.member.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  
  private final AuthService authService;
  private final EmailService emailService;

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
    // 로그인 처리 및 토큰 생성
    String token = authService.login(request);

    // 사용자 정보를 가져오기
    Member member = authService.getMemberByEmail(request.getEmail());
    LoginResponse loginResponse = new LoginResponse(
        member.getEmail(),
        member.getName(),
        member.getNickname(),
        member.getPhone()
        
        );

    // HttpHeaders를 사용하여 헤더에 Authorization 추가
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    // ResponseEntity에 헤더와 바디를 추가
    return ResponseEntity.ok()
        .headers(headers)
        .body(loginResponse);
  }

  // 로그아웃
  @PostMapping("/sign-out")
  public ResponseEntity<Void> logout(@LoginMemberId Long memberId) {
    authService.logout(memberId); // Redis에서 토큰 삭제
    return ResponseEntity.ok().build();
  }


  // 비밀번호 재설정
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(
      @RequestBody @Valid ResetPasswordRequest request
  ) {
      authService.resetPassword(request.getEmail(), request);
      return ResponseEntity.ok().build();
  }

  // 이메일 중복 확인
  @GetMapping("/check-email")
  public ResponseEntity<Void> checkEmail(@RequestParam("email") String email) {
    emailService.validateEmail(email);
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
    emailService.verifyEmailToken(token);
    return ResponseEntity.ok().build();
  }

  // 이메일 인증 요청
  @PostMapping(value = "/send-verification-email", produces = "application/json")
  public ResponseEntity<Void> sendVerificationEmail(
      @RequestParam(name = "email") String email
  ) {
    emailService.sendVerificationEmail(email);
    return ResponseEntity.ok().build();
  }
}



