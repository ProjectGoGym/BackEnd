package com.gogym.chat.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.event.ChatRoomEvent;
import com.gogym.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomEventListener {
  
  private final ChatMessageService chatMessageService;
  
  @EventListener
  public void handleChatRoomCreated(ChatRoomEvent event) {
    // 초기 메시지 전송
    this.chatMessageService.sendMessage(
        new ChatMessageRequest(
            event.getChatRoomId(),
            String.format("%s님이 채팅을 요청하였습니다.", event.getRequestorNickname())
        ),
        event.getRequestorId()
    );
  }
  
}
