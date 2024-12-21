package com.gogym.gympay.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.gympay.dto.request.UpdateDateRequest;
import com.gogym.gympay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions/")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  @PutMapping("/{chat-room-id}/date")
  public ResponseEntity<Void> updateDate(@LoginMemberId Long memberId,
      @PathVariable("chat-room-id") Long chatRoomId,
      @RequestBody UpdateDateRequest request) {
    transactionService.patchDate(memberId, chatRoomId, request);

    return ResponseEntity.noContent().build();
  }
}
