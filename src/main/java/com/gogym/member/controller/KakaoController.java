package com.gogym.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gogym.member.dto.KakaoLoginResponse;
import com.gogym.member.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class KakaoController {

  private final KakaoService kakaoService;

  @GetMapping("/sign-in")
  public ResponseEntity<Boolean> handleKakaoLogin(@RequestParam("code") String code) {
    KakaoLoginResponse response = kakaoService.processKakaoLogin(code);

    HttpHeaders headers = new HttpHeaders();
    if (response.getToken() != null) { // 토큰이 존재하는 경우에만 헤더 추가하고
      headers.add("Authorization", "Bearer " + response.getToken());
    }

    // 신규/기존 여부만 본문으로 반환
    return ResponseEntity.ok().headers(headers).body(response.isExistingUser());
  }
  
}
