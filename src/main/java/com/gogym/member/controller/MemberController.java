package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.dto.MemberDto;
import com.gogym.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 계정 및 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  /**
   * 회원가입 API
   *
   * @param request 회원가입 요청 데이터
   * @return 성공 응답
   */
  @PostMapping("/sign-up")
  public ResponseEntity<ApplicationResponse<Void>> signUp(@RequestBody @Valid MemberDto.SignUpRequest request) {
    memberService.signUp(request);
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.SUCCESS));
  }

  /**
   * 로그인 API
   *
   * @param request 로그인 요청 데이터
   * @return 로그인 응답 데이터 (닉네임, JWT 토큰)
   */
  @PostMapping("/sign-in")
  public ResponseEntity<ApplicationResponse<MemberDto.LoginResponse>> signIn(@RequestBody @Valid MemberDto.LoginRequest request) {
    MemberDto.LoginResponse response = memberService.signIn(request);
    return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.SUCCESS));
  }

  /**
   * 로그아웃 API
   *
   * @param token 클라이언트의 JWT 토큰
   * @return 성공 응답
   */
  @PostMapping("/sign-out")
  public ResponseEntity<ApplicationResponse<Void>> signOut(@RequestHeader("Authorization") String token) {
    // 토큰에서 "Bearer " 없애기
    String jwtToken = token.replace("Bearer ", "");
    memberService.signOut(jwtToken);
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.SUCCESS));
  }

  /**
   * 비밀번호 재설정 API
   *
   * @param request 비밀번호 재설정 요청 데이터
   * @return 성공 응답
   */
  @PostMapping("/reset-password")
  public ResponseEntity<ApplicationResponse<Void>> resetPassword(@RequestBody @Valid MemberDto.ResetPasswordRequest request) {
    memberService.resetPassword(request);
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.SUCCESS));
  }

  /**
   * 이메일 중복 확인 API
   *
   * @param email 확인할 이메일
   * @return 중복 여부 응답 (true : 사용 가능, false : 중복)
   */
  @GetMapping("/check-email")
  public ResponseEntity<ApplicationResponse<Boolean>> checkEmail(@RequestParam("email") String email) {
    boolean isAvailable = memberService.checkEmail(email);
    return ResponseEntity.ok(ApplicationResponse.ok(isAvailable, SuccessCode.SUCCESS));
  }

  /**
   * 닉네임 중복 확인 API
   *
   * @param nickname 확인할 닉네임
   * @return 중복 여부 응답 (true : 사용 가능, false : 중복)
   */
  @GetMapping("/check-nickname")
  public ResponseEntity<ApplicationResponse<Boolean>> checkNickname(@RequestParam(name = "nickname") String nickname) {
    boolean isAvailable = memberService.checkNickname(nickname);
    return ResponseEntity.ok(ApplicationResponse.ok(isAvailable, SuccessCode.SUCCESS));
  }

  /**
   * 이메일 인증 링크 확인 API
   *
   * @param token 이메일 인증 토큰
   * @return 성공 메시지 응답
   */
  @GetMapping("/verify-email")
  public ResponseEntity<ApplicationResponse<String>> verifyEmail(@RequestParam String token) {
    memberService.verifyEmailToken(token);
    return ResponseEntity.ok(ApplicationResponse.ok("이메일 인증이 완료되었습니다.", SuccessCode.SUCCESS));
  }
}



