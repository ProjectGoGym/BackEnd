package com.gogym.gympay.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.gympay.service.GymPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gym-pays")
public class GymPayController {

  private final GymPayService gymPayService;

  @PostMapping
  public ResponseEntity<Long> open(@LoginMemberId Long memberId) {
    gymPayService.save(memberId);

    return ResponseEntity.noContent().build();
  }
}
