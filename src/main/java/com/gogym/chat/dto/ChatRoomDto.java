package com.gogym.chat.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
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
      LocalDateTime lastMessageAt,
      boolean postAuthorActive,
      boolean requestorActive) {}

  public record LeaveRequest(
      @NotNull
      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
      LocalDateTime leaveAt) {}

}
