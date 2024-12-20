package com.gogym.chat.dto;

public interface MessageRequest {
  Long chatRoomId();
  String content();
  Long senderId();
}
