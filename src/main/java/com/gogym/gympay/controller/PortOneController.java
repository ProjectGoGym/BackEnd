package com.gogym.gympay.controller;

import static com.gogym.common.response.SuccessCode.SUCCESS;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.gympay.service.PortOneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/iamport")
public class PortOneController {

  private final PortOneService portOneService;

  @GetMapping("/token")
  public ResponseEntity<ApplicationResponse<String>> getToken() {
    var tokens = portOneService.getTokens();

    ResponseCookie refreshTokenCookie = ResponseCookie.from("Iamport-Refresh-Token", tokens.refreshToken())
        .httpOnly(true)
        // TODO: HTTPS 사용하면 활성화
//        .secure(true)
        .path("/")
        .maxAge(7 * 24 * 60 * 60)
        .build();

    return ResponseEntity.ok()
        .header("Set-Cookie", refreshTokenCookie.toString())
        .body(ApplicationResponse.ok(tokens.accessToken(), SUCCESS));
  }
}