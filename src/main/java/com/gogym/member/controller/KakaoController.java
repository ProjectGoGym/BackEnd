package com.gogym.member.controller;

import com.gogym.member.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
@Slf4j
public class KakaoController {

  private final KakaoService kakaoService;

  @GetMapping("/callback")
  public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) {
    // 카카오 로그인 처리 및 JWT 토큰 생성
    kakaoService.handleKakaoCallback(code);
    return ResponseEntity.ok("카카오 로그인 성공");
  }

}
