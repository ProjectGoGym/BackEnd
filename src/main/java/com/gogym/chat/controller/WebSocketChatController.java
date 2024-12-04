package com.gogym.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.service.ChatRedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket을 통한 실시간 채팅 메시지 처리를 담당하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
  
  private static final String TOPIC_CHATROOM_PREFIX = "/topic/chatroom/";
  
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatRedisService chatRedisService;
  
  /**
   * 실시간 채팅 메시지를 처리하고 Redis에 저장한 뒤,
   * 해당 채팅방의 구독자에게 브로드캐스트합니다.
   * 
   * 클라이언트는 "/app/chatroom/message" 경로로 메시지를 보냅니다.
   * 
   * @param request 송신 메시지 정보를 포함한 {@link ChatMessageRequest}.
   */
  @MessageMapping("/chatroom/message")
  public void send(@Valid ChatMessageRequest messageRequest) {
    // 메시지를 Redis에 저장
    ChatMessageResponse savedMessage = this.chatRedisService.saveMessageToRedis(messageRequest);
    
    // 메시지를 구독자에게 전송
    this.messagingTemplate.convertAndSend(
        TOPIC_CHATROOM_PREFIX + messageRequest.chatRoomId(),
        savedMessage);
  }
  
}
