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
      String counterpartyProfileImageUrl,
      int unreadMessageCount,
      String lastMessage,
      LocalDateTime lastMessageAt,
      boolean postAuthorActive,
      boolean requestorActive
  ) {}

  public record LeaveRequest(
      @NotNull LocalDateTime leaveAt
  ) {}

}
