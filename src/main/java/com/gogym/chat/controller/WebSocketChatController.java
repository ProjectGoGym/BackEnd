package com.gogym.chat.controller;

import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket을 통한 실시간 채팅 메시지 처리를 담당하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
  
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatRedisService chatRedisService;
  private final ChatRoomService chatRoomService;
  
  private static final String TOPIC_CHATROOM_PREFIX = "/topic/chatroom/";
  
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
    
    // 메시지를 Redis에 저장
    ChatMessageResponse savedMessage = this.chatRedisService.saveMessageToRedis(messageRequest, memberId);
    
    // 메시지 브로드캐스트
    this.messagingTemplate.convertAndSend(
        TOPIC_CHATROOM_PREFIX + messageRequest.chatRoomId(),
        savedMessage);
  }
  
}
