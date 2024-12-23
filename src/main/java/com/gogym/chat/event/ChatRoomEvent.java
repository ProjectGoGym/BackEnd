package com.gogym.chat.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class ChatRoomEvent extends ApplicationEvent {
  
  private final Long chatRoomId;
  private final Long requestorId;
  private final String requestorNickname;
  
  public ChatRoomEvent(Object source, Long chatRoomId, Long requestorId, String requestorNickname) {
    super(source);
    this.chatRoomId = chatRoomId;
    this.requestorId = requestorId;
    this.requestorNickname = requestorNickname;
  }
  
}
