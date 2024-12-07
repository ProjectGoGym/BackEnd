package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.util.JsonUtil;
import com.gogym.util.RedisUtil;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService {

  private final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final RedisUtil redisUtil;

  @Override
  public ChatMessageResponse saveMessageToRedis(ChatMessageRequest messageRequest) {
    // Redis Key 생성
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + messageRequest.chatRoomId();

    // LocalDateTime을 String으로 변환
    String createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);

    // Redis에 저장할 메시지 객체 생성
    ChatMessageHistory messageHistory = new ChatMessageHistory(
        messageRequest.content(),
        messageRequest.senderId(),
        createdAt);

    // 메시지 객체를 JSON 문자열로 직렬화
    String messageJson = JsonUtil.serialize(messageHistory);

    // Redis에 저장
    this.redisUtil.lpush(redisKey, messageJson);

    // 메시지 저장 결과 반환
    return new ChatMessageResponse(
        messageRequest.chatRoomId(),
        messageRequest.senderId(),
        messageRequest.content(),
        LocalDateTime.parse(createdAt, DATE_TIME_FORMATTER));
  }

  @Override
  public String getRedisChatroomMessageKeyPrefix() {
    return this.REDIS_CHATROOM_MESSAGE_KEY;
  }

}
