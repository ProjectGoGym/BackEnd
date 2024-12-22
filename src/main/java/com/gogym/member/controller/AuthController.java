package com.gogym.member.controller;

import com.gogym.member.dto.LoginResponse;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.service.AuthService;
import com.gogym.member.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final EmailService emailService;

  // 회원가입
  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(@RequestBody @Valid SignUpRequest request,
      @RequestParam(name = "isKakao", defaultValue = "false") boolean isKakao) {
    authService.signUp(request, isKakao);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  // 로그인
  @PostMapping("/sign-in")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid SignInRequest request) {
    // 로그인 처리 및 Access Token, Refresh Token 생성
    Map<String, String> tokens = authService.login(request);
    String accessToken = tokens.get("accessToken");
    String refreshToken = tokens.get("refreshToken");

    // 사용자 정보 가져오기
    Member member = authService.getMemberByEmail(request.getEmail());
    LoginResponse loginResponse = new LoginResponse(member.getId(), member.getEmail(),
        member.getName(), member.getNickname(), member.getPhone());

    // HttpHeaders에 Access Token과 Refresh Token 추가
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);
    headers.add("Refresh-Token", refreshToken);

    // ResponseEntity에 헤더와 바디를 추가
    return ResponseEntity.ok().headers(headers).body(loginResponse);
  }

  // 로그아웃
  @PostMapping("/sign-out")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    authService.logout(request);
    return ResponseEntity.ok().build();
  }

  // 비밀번호 재설정
  @PutMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(HttpServletRequest request,
      @RequestBody @Valid ResetPasswordRequest resetRequest) {
    authService.resetPassword(request, resetRequest);
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
  public ResponseEntity<Void> verifyEmail(@RequestParam(name = "token") String token)
      throws URISyntaxException {
    emailService.verifyEmailToken(token);

    URI redirectUri = new URI("https://gogym-eight.vercel.app");
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(redirectUri);
    // 301 Moved Permanently 상태 코드로 리다이렉트
    return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
  }

  // 이메일 인증 요청
  @PostMapping(value = "/send-verification-email", produces = "application/json")
  public ResponseEntity<Void> sendVerificationEmail(@RequestParam(name = "email") String email) {
    emailService.sendVerificationEmail(email);
    return ResponseEntity.ok().build();
  }

  // 토큰 갱신
  @PostMapping("/refresh-token")
  public ResponseEntity<Map<String, String>> refreshAccessToken(
      @RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");
    String newAccessToken = authService.refreshAccessToken(refreshToken);

    Map<String, String> response = new HashMap<>();
    response.put("accessToken", newAccessToken);
    return ResponseEntity.ok(response);
  }
}
