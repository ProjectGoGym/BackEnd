package com.gogym.chat.dto.base;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.dto.ChatMessageDto.SafePaymentRedisMessage;
import com.gogym.chat.type.MessageType;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SafePaymentRedisMessage.class, name = "SafePaymentRedisMessage"),
    @JsonSubTypes.Type(value = RedisChatMessage.class, name = "RedisChatMessage")}
)
public interface RedisMessage {
  String content();
  Long senderId();
  MessageType messageType();
  LocalDateTime createdAt();
}
