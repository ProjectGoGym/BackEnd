package com.gogym.chat.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

public class ChatRoomDto {

  public record ChatRoomResponse(
      Long chatRoomId,
      LocalDateTime createdAt,
      Long postId,
      Long counterpartyId,
      String counterpartyNickname,
      int unreadMessageCount,
      String lastMessage,
      LocalDateTime lastMessageAt) {}

  public record LeaveRequest(@NotNull Long lastReadMessageId) {}

}
