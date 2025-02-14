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
@RequestMapping("/api/chatrooms")
public class SafePaymentController {

  private final SafePaymentService safePaymentService;

  @PostMapping("/{chatroom-id}/safe-payments")
  public ResponseEntity<Long> create(@LoginMemberId Long requesterId,
      @PathVariable("chatroom-id") Long chatRoomId,
      @RequestBody SafePaymentRequest request) {

    Long safePaymentId = safePaymentService.save(requesterId, chatRoomId, request.amount());

    return ResponseEntity.ok(safePaymentId);
  }

  @PutMapping("/{chatroom-id}/safe-payments/{safe-payment-id}/{status}")
  public ResponseEntity<Void> changeStatus(@PathVariable("chatroom-id") Long chatRoomId,
      @PathVariable("safe-payment-id") Long safePaymentId,
      @PathVariable String status,
      @LoginMemberId Long requesterId) {
    safePaymentService.changeStatus(chatRoomId, safePaymentId, status, requesterId);

    return ResponseEntity.noContent().build();
  }
}