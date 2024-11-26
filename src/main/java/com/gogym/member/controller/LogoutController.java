package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.service.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/logout")
@RequiredArgsConstructor
public class LogoutController {
  //로그아웃 처리 비즈니스 로직을 담당하는 서비스
  private final LogoutService logoutService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<Void>> logout(
    @RequestHeader("Authorization") String token // Authorization 헤더에서 토큰 수신
  ) {
    logoutService.logout(token.replace("Bearer ", "")); // 토큰의 "Bearer " 접두사 제거 후 로그아웃 처리
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.LOGOUT_SUCCESS)); // 성공 응답 반환
  }
}

