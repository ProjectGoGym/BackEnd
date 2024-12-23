package com.gogym.chat.dto.base;

import java.time.LocalDateTime;
import com.gogym.chat.type.MessageType;

public interface MessageResponse {
  Long chatRoomId();
  String content();
  Long senderId();
  MessageType messageType();
  LocalDateTime createdAt();
}
