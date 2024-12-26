package com.gogym.chat.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gogym.chat.dto.ChatMessageDto.ChatRoomMessagesResponse;
import com.gogym.chat.service.ChatMessageQueryService;
import com.gogym.common.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatMessageController {
  
  private final ChatMessageQueryService chatMessageQueryService;
  
  /**
   * 특정 채팅방의 메시지와 해당 채팅방에 연결된 게시물 정보를 조회합니다.
   * 
   * GET /api/chatroom/{chatroom-id}/messages?page={page}&size={size}
   * 
   * @param memberId 요청 사용자 ID
   * @param chatRoomId 조회할 채팅방 ID
   * @param pageable 페이지네이션 정보 (page, size)
   * @return {@link ChatRoomMessagesResponse} 메시지 목록과 연결된 게시물 정보
   */
  @GetMapping("/{chatroom-id}/messages")
  public ResponseEntity<ChatRoomMessagesResponse> getMessagesInChatroom(
      @LoginMemberId Long memberId,
      @PathVariable("chatroom-id") Long chatRoomId,
      Pageable pageable) {
    return ResponseEntity.ok(this.chatMessageQueryService.getChatRoomMessagesAndPostInfo(memberId, chatRoomId, pageable));
  }
  
}
