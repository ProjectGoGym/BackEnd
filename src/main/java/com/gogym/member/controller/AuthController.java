package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.AuthResponse;
import com.gogym.member.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final String ROLE_USER = "ROLE_USER";

  private final AuthService authService;

  // 회원가입
  @PostMapping("/sign-up")
  public ResponseEntity<ApplicationResponse<AuthResponse>> signUp(
    @RequestBody @Valid SignUpRequest request // 요청 데이터 검증 및 전달
  ) {
    AuthResponse response = authService.signUp(request); // 회원가입 처리
    return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.AUTH_SUCCESS)); // 성공 응답 반환
  }

  // 로그인
  @PostMapping("/sign-in")
  public ResponseEntity<ApplicationResponse<AuthResponse>> login(
    @RequestBody @Valid SignInRequest request
  ) {
    // 로그인 처리 및 사용자 정보 생성
    AuthResponse response = authService.login(request);

    // JWT 토큰 생성
    String token = authService.generateToken(request.getEmail(), List.of(ROLE_USER));

    // 응답
    return ResponseEntity.ok()
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .body(ApplicationResponse.ok(response, SuccessCode.AUTH_SUCCESS));
  }

  // 로그아웃
  @PostMapping("/sign-out")
  public ResponseEntity<ApplicationResponse<Void>> logout(
    @RequestHeader("Authorization") String token
  ) {
    authService.logout(token.replace("Bearer ", ""));
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.AUTH_SUCCESS));
  }

  // 비밀번호 재설정
  @PostMapping("/reset-password")
  public ResponseEntity<ApplicationResponse<Void>> resetPassword(
    @RequestBody @Valid ResetPasswordRequest request // 요청 데이터 검증 및 전달
  ) {
    authService.resetPassword(request); // 비밀번호 재설정 처리
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.AUTH_SUCCESS)); // 성공 응답 반환
  }

  // 이메일 중복 확인
  @GetMapping("/check-email")
  public ApplicationResponse<Void> checkEmail(@RequestParam("email") String email) {
    // 이메일 중복 확인 서비스 호출
    authService.checkEmail(email);

    // 성공 응답 반환
    return ApplicationResponse.noData(SuccessCode.DATA_AVAILABLE);
  }

  // 닉네임 중복 확인
  @GetMapping("/check-nickname")
  public ApplicationResponse<Void> checkNickname(@RequestParam("nickname") String nickname) {
    // 닉네임 중복 확인 서비스 호출
    authService.checkNickname(nickname);

    // 성공 응답 반환
    return ApplicationResponse.noData(SuccessCode.DATA_AVAILABLE);
  }

  // 이메일 인증 확인
  @GetMapping("/verify-email")
  public ApplicationResponse<Void> verifyEmail(@RequestParam @NotBlank String token) {
    // 이메일 인증 토큰 검증 서비스 호출
    authService.verifyEmailToken(token);

    // 성공 응답 반환
    return ApplicationResponse.noData(SuccessCode.EMAIL_VERIFIED);
  }

  // 이메일 인증 요청
  @PostMapping("/send-verification-email")
  public ResponseEntity<ApplicationResponse<Void>> sendVerificationEmail(
    @RequestParam @Email String email
  ) {
    authService.sendVerificationEmail(email);
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.AUTH_SUCCESS));
  }
}



