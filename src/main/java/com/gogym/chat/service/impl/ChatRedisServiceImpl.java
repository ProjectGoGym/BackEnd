package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.dto.ChatMessageDto.SafePaymentMessageResponse;
import com.gogym.chat.dto.ChatMessageDto.SafePaymentRedisMessage;
import com.gogym.chat.dto.base.MessageResponse;
import com.gogym.chat.dto.base.RedisMessage;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.type.MessageType;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import com.gogym.util.JsonUtil;
import com.gogym.util.RedisService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService {
  
  private final RedisService redisService;
  
  private final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";
  
  @Override
  public MessageResponse saveMessageToRedis(
      ChatMessageRequest messageRequest,
      Long memberId,
      MessageType messageType,
      Long safePaymentId,
      SafePaymentStatus safePaymentStatus) {
    // Redis Key 생성
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + messageRequest.chatRoomId();

    // 현재 시간 설정
    LocalDateTime createdAt = LocalDateTime.now();

    // Redis에 저장할 메시지 객체 선언
    RedisMessage messageHistory;

    // 메시지 객체를 JSON 문자열로 직렬화할 변수 선언
    String messageJson;

    if (messageType.toString().startsWith("SYSTEM_SAFE_PAYMENT")) {
      // 안전결제 메시지 처리
      messageHistory = new SafePaymentRedisMessage(
          messageRequest.content(),
          memberId,
          messageType,
          createdAt,
          safePaymentId,
          safePaymentStatus
      );
      messageJson = JsonUtil.serialize(messageHistory);

      // Redis 저장
      this.redisService.rpush(redisKey, messageJson);

      // 안전결제 메시지 응답 반환
      return new SafePaymentMessageResponse(
          messageRequest.chatRoomId(),
          memberId,
          messageRequest.content(),
          messageType,
          createdAt,
          safePaymentId,
          safePaymentStatus
      );
    }

    // 일반 메시지 처리
    messageHistory = new RedisChatMessage(
        messageRequest.content(),
        memberId,
        messageType,
        createdAt
    );
    messageJson = JsonUtil.serialize(messageHistory);

    // Redis 저장
    this.redisService.rpush(redisKey, messageJson);

    // 일반 메시지 응답 반환
    return new ChatMessageResponse(
        messageRequest.chatRoomId(),
        memberId,
        messageRequest.content(),
        messageType,
        createdAt
    );
  }
  
  @Override
  public List<String> getMessages(Long chatRoomId) {
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + chatRoomId;
    return this.redisService.lrange(redisKey, 0, -1);
  }
  
  @Override
  public void deleteMessages(Long chatRoomId) {
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + chatRoomId;
    this.redisService.delete(redisKey);
  }

  @Override
  public String getRedisChatroomMessageKeyPrefix() {
    return this.REDIS_CHATROOM_MESSAGE_KEY;
  }

}
