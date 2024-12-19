package com.gogym.gympay.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.gympay.dto.request.SafePaymentRequest;
import com.gogym.gympay.service.SafePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/safe-payments")
public class SafePaymentController {

  private final SafePaymentService safePaymentService;

  @PostMapping("/{chat-room-id}")
  public ResponseEntity<Long> create(@LoginMemberId Long requesterId,
      @PathVariable("chat-room-id") Long chatRoomId,
      @RequestBody SafePaymentRequest request) {
    Long safePaymentId = safePaymentService.save(requesterId, chatRoomId, request);

    return ResponseEntity.ok(safePaymentId);
  }

  @PutMapping("/{safe-payment-id}/approve")
  public ResponseEntity<Void> approve(@PathVariable("safe-payment-id") Long safePaymentId,
      @LoginMemberId Long requesterId) {
    safePaymentService.approve(safePaymentId, requesterId);

    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{safe-payment-id}/reject")
  public ResponseEntity<Void> reject(@PathVariable("safe-payment-id") Long safePaymentId,
      @LoginMemberId Long requesterId) {
    safePaymentService.reject(safePaymentId, requesterId);

    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{safe-payment-id}/complete")
  public ResponseEntity<Void> complete(@PathVariable("safe-payment-id") Long safePaymentId,
      @LoginMemberId Long requesterId) {
    safePaymentService.complete(safePaymentId, requesterId);

    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{safe-payment-id}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable("safe-payment-id") Long safePaymentId,
      @LoginMemberId Long requesterId) {
    safePaymentService.cancel(safePaymentId, requesterId);

    return ResponseEntity.noContent().build();
  }
}
