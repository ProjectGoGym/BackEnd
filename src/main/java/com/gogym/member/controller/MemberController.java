package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.exception.CustomException;
import com.gogym.member.dto.MemberDto;
import com.gogym.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//사용자 계정 및 인증 관련 API 컨트롤러
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  //회원가입 API
  public ResponseEntity<ApplicationResponse<MemberDto.SignUpResponse>> signUp(@RequestBody @Valid MemberDto.SignUpRequest request) {
    MemberDto.SignUpResponse response = memberService.signUp(request);
    return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.SIGN_UP_SUCCESS));
  }

  //로그인 API
  @PostMapping("/sign-in")
  public ResponseEntity<ApplicationResponse<MemberDto.LoginResponse>> signIn(@RequestBody @Valid MemberDto.LoginRequest request) {
      MemberDto.LoginResponse response = memberService.signIn(request);
      return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.LOGIN_SUCCESS));
  }

  //로그아웃 API
  @PostMapping("/sign-out")
  public ResponseEntity<ApplicationResponse<Void>> signOut(@RequestHeader("Authorization") String token) {
      String jwtToken = token.replace("Bearer ", "");
      memberService.signOut(jwtToken);
      return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.LOGOUT_SUCCESS));
  }

  // 비밀번호 재설정 API
  @PostMapping("/reset-password")
  public ResponseEntity<ApplicationResponse<Void>> resetPassword(@RequestBody @Valid MemberDto.ResetPasswordRequest request) {
      memberService.resetPassword(request);
      return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.PASSWORD_RESET_SUCCESS));
  }

  // 이메일 중복확인 API
  @GetMapping("/check-email")
  public ResponseEntity<ApplicationResponse<Boolean>> checkEmail(@RequestParam("email") String email) {
      boolean isAvailable = memberService.checkEmail(email);
      if (isAvailable) {
          return ResponseEntity.ok(
              ApplicationResponse.ok(true, SuccessCode.EMAIL_AVAILABLE)
          );
      } else {
          return ResponseEntity.ok(
              ApplicationResponse.ok(false, SuccessCode.DUPLICATE_EMAIL)
          );
      }
  }

  // 닉네임 중복 확인 API
  @GetMapping("/check-nickname")
  public ResponseEntity<ApplicationResponse<Boolean>> checkNickname(@RequestParam(name = "nickname") String nickname) {
    boolean isAvailable = memberService.checkNickname(nickname);
    return ResponseEntity.ok(ApplicationResponse.ok(isAvailable, SuccessCode.SUCCESS));
  }

  // 이메일 인증 링크 확인 API
  @GetMapping("/verify-email")
  public ResponseEntity<ApplicationResponse<Void>> verifyEmail(@RequestParam String token) {
      try {
          memberService.verifyEmailToken(token);
          return ResponseEntity.ok(
              ApplicationResponse.noData(SuccessCode.EMAIL_VERIFICATION_SUCCESS)
          );
      } catch (CustomException e) {
          // 토큰이 유효하지 않거나 만료된 경우
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(
                  ApplicationResponse.error(e.getErrorCode())
              );
      }
  }

}



