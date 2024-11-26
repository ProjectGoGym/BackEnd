package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.service.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {

  private final ResetPasswordService resetPasswordService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<Void>> resetPassword(
    @RequestBody @Valid ResetPasswordRequest request
  ) {
    resetPasswordService.resetPassword(request);
    return ResponseEntity.ok(ApplicationResponse.noData(SuccessCode.PASSWORD_RESET_SUCCESS));
  }
}
