package com.gogym.chat.dto.base;

import java.time.LocalDateTime;
import com.gogym.chat.type.MessageType;

public interface RedisMessage {
  String content();
  Long senderId();
  MessageType messageType();
  LocalDateTime createdAt();
}
