package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.AuthResponse;
import com.gogym.member.service.AuthService;
import jakarta.validation.Valid;
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
  private static final String SUCCESS_CODE = "200";
  private static final String SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";
  private static final String DATA_AVAILABLE_MESSAGE = "요청하신 데이터는 사용 가능합니다.";
  private static final String EMAIL_VERIFIED_MESSAGE = "이메일 인증이 완료되었습니다.";

  private final AuthService authService;

  // 회원가입
  @PostMapping("/sign-up")
  public ResponseEntity<ApplicationResponse<AuthResponse>> signUp(
    @RequestBody @Valid SignUpRequest request
  ) {
    AuthResponse response = authService.signUp(request);
    return ResponseEntity.ok(ApplicationResponse.ok(response, SUCCESS_CODE, SUCCESS_MESSAGE));
  }

  // 로그인
  @PostMapping("/sign-in")
  public ResponseEntity<ApplicationResponse<AuthResponse>> login(
    @RequestBody @Valid SignInRequest request
  ) {
    AuthResponse response = authService.login(request);
    String token = authService.generateToken(request.getEmail(), List.of(ROLE_USER));

    return ResponseEntity.ok()
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .body(ApplicationResponse.ok(response, SUCCESS_CODE, SUCCESS_MESSAGE));
  }

  // 로그아웃
  @PostMapping("/sign-out")
  public ResponseEntity<ApplicationResponse<Void>> logout(
    @RequestHeader("Authorization") String token
  ) {
    authService.logout(token.replace("Bearer ", ""));
    return ResponseEntity.ok(ApplicationResponse.noData(SUCCESS_CODE, SUCCESS_MESSAGE));
  }

  // 비밀번호 재설정
  @PostMapping("/reset-password")
  public ResponseEntity<ApplicationResponse<Void>> resetPassword(
    @RequestBody @Valid ResetPasswordRequest request
  ) {
    authService.resetPassword(request);
    return ResponseEntity.ok(ApplicationResponse.noData(SUCCESS_CODE, SUCCESS_MESSAGE));
  }

  // 이메일 중복 확인
  @GetMapping("/check-email")
  public ApplicationResponse<Void> checkEmail(@RequestParam("email") String email) {
    authService.checkEmail(email);
    return ApplicationResponse.noData(SUCCESS_CODE, DATA_AVAILABLE_MESSAGE);
  }

  // 닉네임 중복 확인
  @GetMapping("/check-nickname")
  public ApplicationResponse<Void> checkNickname(@RequestParam("nickname") String nickname) {
    authService.checkNickname(nickname);
    return ApplicationResponse.noData(SUCCESS_CODE, DATA_AVAILABLE_MESSAGE);
  }

  // 이메일 인증 확인
  @GetMapping("/verify-email")
  public ApplicationResponse<Void> verifyEmail(@RequestParam(name = "token") String token) {
    authService.verifyEmailToken(token);
    return ApplicationResponse.noData(SUCCESS_CODE, EMAIL_VERIFIED_MESSAGE);
  }

  // 이메일 인증 요청
  @PostMapping(value = "/send-verification-email", produces = "application/json")
  public ResponseEntity<ApplicationResponse<Void>> sendVerificationEmail(
      @RequestParam(name = "email") String email
  ) {
    authService.sendVerificationEmail(email);
    return ResponseEntity.ok(ApplicationResponse.noData(SUCCESS_CODE, SUCCESS_MESSAGE));
  }
}




