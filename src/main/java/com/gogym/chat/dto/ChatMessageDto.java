package com.gogym.chat.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {

  public record ChatMessageRequest(
      Long chatroomId,
      Long senderId,
      String content) {}

  public record ChatMessageResponse(
      Long chatroomId,
      Long senderId,
      String content,
      LocalDateTime createdAt) {}

  public record ChatMessageHistory(
      String content,
      Long senderId,
      String createdAt) {}

}
