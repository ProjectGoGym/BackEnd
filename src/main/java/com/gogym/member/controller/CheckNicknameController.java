package com.gogym.member.controller;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.common.response.SuccessCode;
import com.gogym.member.service.CheckNicknameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/check-nickname")
@RequiredArgsConstructor
public class CheckNicknameController {

  private final CheckNicknameService checkNicknameService;

  @GetMapping
  public ResponseEntity<ApplicationResponse<Boolean>> checkNickname(
    @RequestParam("nickname") String nickname
  ) {
    boolean isAvailable = checkNicknameService.checkNickname(nickname);
    return ResponseEntity.ok(
      ApplicationResponse.ok(
        isAvailable,
        isAvailable ? SuccessCode.NICKNAME_AVAILABLE : SuccessCode.DUPLICATE_NICKNAME
      )
    );
  }
}
