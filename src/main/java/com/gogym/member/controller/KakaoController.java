package com.gogym.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
  public ResponseEntity<Object> handleKakaoLogin(@RequestParam("code") String code,
      HttpServletRequest request) {
    String currentDomain = request.getRequestURL().toString().replace(request.getRequestURI(), "");
    String token = kakaoService.processKakaoLogin(code, currentDomain); //카카오 로그인 처리

    if (token == null) {
      return ResponseEntity.ok(false); // 회원 정보가 없거나 isKakao == false: 클라이언트에 false 반환
    }

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);
    return ResponseEntity.ok().headers(headers).body(true);
  }
}
