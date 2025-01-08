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
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.gogym.member.dto.KakaoLoginResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class KakaoController {

  private final KakaoService kakaoService;

  @GetMapping("/sign-in")
  public ResponseEntity<KakaoLoginResponse> handleKakaoLogin(@RequestParam("code") String code) {
    KakaoLoginResponse response = kakaoService.processKakaoLogin(code);

    HttpHeaders headers = new HttpHeaders();
    String token = kakaoService.generateJwtToken(response.getEmail());
    if (token != null) {
      headers.add("Authorization", "Bearer " + token);
    }

    // 응답 본문은 email과 existingUser 정보만 반환
    return ResponseEntity.ok().headers(headers).body(response);
  }

}
