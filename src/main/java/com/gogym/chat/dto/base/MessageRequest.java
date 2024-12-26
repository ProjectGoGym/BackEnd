package com.gogym.chat.dto.base;

public interface MessageRequest {
  Long chatRoomId();
  String content();
  Long senderId();
}
