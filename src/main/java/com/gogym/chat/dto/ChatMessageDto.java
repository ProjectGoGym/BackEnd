package com.gogym.chat.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageDto {

  public record ChatMessageRequest(
      @NotNull Long chatRoomId,
      @NotBlank String content) {}

  public record ChatMessageResponse(
      Long chatRoomId,
      Long senderId,
      String content,
      LocalDateTime createdAt) {}

  public record ChatMessageHistory(
      String content,
      Long senderId,
      LocalDateTime createdAt) {}

}
