package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.dto.SignUpResponse;
import com.gogym.member.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/sign-up")
@RequiredArgsConstructor
public class SignUpController {

  private final SignUpService signUpService;

  @PostMapping
  public ResponseEntity<ApplicationResponse<SignUpResponse>> signUp(
    @RequestBody @Valid SignUpRequest request
  ) {
    SignUpResponse response = signUpService.signUp(request);
    return ResponseEntity.ok(ApplicationResponse.ok(response, SuccessCode.SIGN_UP_SUCCESS));
  }
}
