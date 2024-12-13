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
  public ResponseEntity<Void> handleKakaoLogin(@RequestParam("code") String code,
      HttpServletRequest request) {
    String currentDomain = request.getRequestURL().toString().replace(request.getRequestURI(), "");
    String token = kakaoService.processKakaoLogin(code, currentDomain);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);
    return ResponseEntity.ok().headers(headers).build();
  }
}
