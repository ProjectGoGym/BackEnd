package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.exception.CustomException;
import com.gogym.member.service.VerifyEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/verify-email")
@RequiredArgsConstructor
public class VerifyEmailController {

  private final VerifyEmailService verifyEmailService;

  @GetMapping
  public ResponseEntity<ApplicationResponse<Void>> verifyEmail(
    @RequestParam String token
  ) {
    try {
      verifyEmailService.verifyEmailToken(token);
      return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.EMAIL_VERIFICATION_SUCCESS));
    } catch (CustomException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApplicationResponse.error(e.getErrorCode()));
    }
  }
}
