package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.service.ChatRedisService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService {

  private static final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public ChatMessageResponse saveMessageToRedis(ChatMessageRequest messageRequest) {
    // Redis Key 생성
    String redisKey = REDIS_CHATROOM_MESSAGE_KEY + messageRequest.chatroomId();

    // LocalDateTime을 String으로 변환
    String createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);

    // Redis에 저장할 메시지 객체 생성
    ChatMessageHistory messageHistory = new ChatMessageHistory(
        messageRequest.content(),
        messageRequest.senderId(),
        createdAt);

    // Redis 목록에 메시지 추가
    this.redisTemplate.opsForList().rightPush(redisKey, messageHistory);

    // 메시지 저장 결과 반환
    return new ChatMessageResponse(
        messageRequest.chatroomId(),
        messageRequest.senderId(),
        messageRequest.content(),
        LocalDateTime.parse(createdAt, DATE_TIME_FORMATTER));
  }

}
