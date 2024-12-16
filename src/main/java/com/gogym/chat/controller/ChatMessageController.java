package com.gogym.chat.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gogym.chat.dto.ChatMessageDto.ChatRoomMessagesResponse;
import com.gogym.chat.service.ChatMessageService;
import com.gogym.common.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatMessageController {
  
  private final ChatMessageService chatMessageService;
  
  @GetMapping("/{chatroom-id}/messages")
  public ResponseEntity<ChatRoomMessagesResponse> getMessagesInChatroom(
      @LoginMemberId Long memberId,
      @PathVariable("chatroom-id") Long chatRoomId,
      Pageable pageable) {
    return ResponseEntity.ok(this.chatMessageService.getMessagesWithPostStatus(chatRoomId, pageable));
  }
  
}
