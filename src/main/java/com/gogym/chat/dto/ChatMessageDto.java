package com.gogym.chat.dto;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import com.gogym.chat.type.MessageType;
import com.gogym.post.type.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageDto {

  public record ChatMessageRequest(
      @NotNull Long chatRoomId,
      @NotBlank String content) implements MessageRequest {
    @Override
    public Long senderId() {
      return null;
    }
  }

  public record ChatMessageResponse(
      Long chatRoomId,
      Long senderId,
      String content,
      MessageType messageType,
      LocalDateTime createdAt) {}

  public record RedisChatMessage(
      String content,
      Long senderId,
      MessageType messageType,
      LocalDateTime createdAt) {}

  public record ChatRoomMessagesResponse(
      Page<ChatMessageResponse> messages,
      PostStatus postStatus,
      LocalDateTime leaveAt) {}

}
