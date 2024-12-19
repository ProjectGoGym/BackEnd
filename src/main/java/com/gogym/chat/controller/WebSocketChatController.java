package com.gogym.chat.controller;

import java.security.Principal;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.service.ChatMessageService;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket을 통한 실시간 채팅 메시지 처리를 담당하는 컨트롤러.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
  
  private final ChatMessageService chatMessageService;
  private final ChatRoomService chatRoomService;
  
  /**
   * 실시간 채팅 메시지를 처리하고 Redis에 저장한 뒤,
   * 해당 채팅방의 구독자에게 브로드캐스트합니다.
   * 
   * 클라이언트는 "/app/chatroom/message" 경로로 메시지를 보냅니다.
   * 
   * @param messageRequest 클라이언트가 전송한 채팅 메시지 요청 데이터
   * @param headerAccessor STOMP 메시지 헤더에 접근하기 위한 접근자
   */
  @CrossOrigin
  @MessageMapping("/chatroom/message")
  public void send(
      @Valid @Payload ChatMessageRequest messageRequest,
      SimpMessageHeaderAccessor headerAccessor) {
    // STOMP 세션에서 인증된 사용자 정보(Principal) 가져오기
    Principal principal = (Principal) headerAccessor.getSessionAttributes().get("principal");
    
    // 사용자 인증이 세션에 유지되고 있는지를 확인하기 위한 방어 로직
    if (principal == null) {
      throw new CustomException(ErrorCode.WEBSOCKET_UNAUTHORIZED);
    }
    
    // 사용자 ID 추출
    Long memberId = Long.valueOf(principal.getName());
    
    // 사용자가 채팅방에 속해있는지 확인하기 위한 방어 로직
    if (!this.chatRoomService.isMemberInChatRoom(messageRequest.chatRoomId(), memberId)) {
      throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // 메시지 브로드캐스팅
    this.chatMessageService.sendMessage(messageRequest, memberId);
    
    // WebSocket 로깅
    String transactionId = UUID.randomUUID().toString().substring(0, 8);
    log.info("[WebSocket] TransactionId: {}, SenderId: {}, ChatRoomId: {}, Content: {}",
        transactionId,
        memberId,
        messageRequest.chatRoomId(),
        messageRequest.content());
  }
  
}
