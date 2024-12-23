package com.gogym.chat.dto;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import com.gogym.chat.dto.base.MessageRequest;
import com.gogym.chat.dto.base.MessageResponse;
import com.gogym.chat.dto.base.RedisMessage;
import com.gogym.chat.type.MessageType;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import com.gogym.post.dto.PostSummaryDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageDto {

  public record ChatMessageRequest(
      @NotNull Long chatRoomId,
      @NotBlank String content
  ) implements MessageRequest {
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
      LocalDateTime createdAt
  ) implements MessageResponse {}

  public record RedisChatMessage(
      String content,
      Long senderId,
      MessageType messageType,
      LocalDateTime createdAt
  ) implements RedisMessage {}

  public record SafePaymentMessageResponse(
      Long chatRoomId,
      Long senderId,
      String content,
      MessageType messageType,
      LocalDateTime createdAt,
      Long safePaymentId,
      SafePaymentStatus safePaymentStatus
  ) implements MessageResponse {}

  public record SafePaymentRedisMessage(
      String content,
      Long senderId,
      MessageType messageType,
      LocalDateTime createdAt,
      Long safePaymentId,
      SafePaymentStatus safePaymentStatus
  ) implements RedisMessage {}

  public record ChatRoomMessagesResponse(
      Page<MessageResponse> messages,
      PostSummaryDto postSummary,
      LocalDateTime leaveAt
  ) {}

}
