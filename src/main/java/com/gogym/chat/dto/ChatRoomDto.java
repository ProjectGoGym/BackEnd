package com.gogym.chat.dto;

import java.time.LocalDateTime;

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

  public record LeaveRequest(Long lastReadMessageId) {}

}
