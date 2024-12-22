package com.gogym.gympay.event;

import com.gogym.chat.dto.MessageRequest;
import com.gogym.chat.type.MessageType;
import com.gogym.gympay.entity.constant.SafePaymentStatus;

public record SendMessageEvent(
    Long chatRoomId,
    Long senderId,
    String content,
    MessageType messageType,
    Long safePaymentId,
    SafePaymentStatus safePaymentstatus) implements MessageRequest {}
