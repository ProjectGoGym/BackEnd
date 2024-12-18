package com.gogym.chat.dto;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import com.gogym.post.type.PostStatus;
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

  public record RedisChatMessage(
      String content,
      Long senderId,
      LocalDateTime createdAt) {}

  public record ChatRoomMessagesResponse(
      Page<ChatMessageResponse> messages,
      PostStatus postStatus,
      LocalDateTime leaveAt) {}

}
