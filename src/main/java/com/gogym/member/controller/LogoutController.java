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

  private final LogoutService logoutService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<Void>> logout(
    @RequestHeader("Authorization") String token
  ) {
    logoutService.logout(token.replace("Bearer ", ""));
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.LOGOUT_SUCCESS));
  }
}
