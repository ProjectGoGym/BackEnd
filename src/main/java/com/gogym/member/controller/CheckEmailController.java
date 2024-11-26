package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.service.CheckEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/check-email")
@RequiredArgsConstructor
public class CheckEmailController {

  private final CheckEmailService checkEmailService;

  @GetMapping
  public ResponseEntity<ApplicationResponse<Boolean>> checkEmail(
    @RequestParam("email") String email
  ) {
    boolean isAvailable = checkEmailService.checkEmail(email);
    return ResponseEntity.ok(
      ApplicationResponse.ok(
        isAvailable,
        isAvailable ? SuccessCode.EMAIL_AVAILABLE : SuccessCode.DUPLICATE_EMAIL
      )
    );
  }
}
