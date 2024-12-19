package com.gogym.gympay.event;

import com.gogym.chat.dto.MessageRequest;
import com.gogym.chat.type.MessageType;

public record SendMessageEvent(
    Long chatRoomId,
    Long senderId,
    String content,
    MessageType messageType) implements MessageRequest {}
