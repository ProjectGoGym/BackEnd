package com.gogym.gympay.controller;

import com.gogym.gympay.service.GymPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gym-pay")
public class GymPayController {

  private final GymPayService gymPayService;

  @PostMapping
  public ResponseEntity<Long> open() {
    Long gymPayId = gymPayService.save(1L);

    return ResponseEntity.ok(gymPayId);
  }
}
