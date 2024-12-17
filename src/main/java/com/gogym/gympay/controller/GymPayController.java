package com.gogym.gympay.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.gympay.dto.request.SafePaymentRequest;
import com.gogym.gympay.service.GymPayService;
import com.gogym.gympay.service.SafePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gym-pays")
public class GymPayController {

  private final GymPayService gymPayService;
  private final SafePaymentService safePaymentService;

  @PostMapping
  public ResponseEntity<Long> open(@LoginMemberId Long memberId) {
    gymPayService.save(memberId);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/safe-payment")
  public ResponseEntity<Long> createSafePayment(@LoginMemberId Long buyerId,
      @RequestBody SafePaymentRequest request) {
    Long transactionId = safePaymentService.save(buyerId, request);

    return null;
  }
}
