package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.util.RedisUtil;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService {

  private static final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final RedisUtil redisUtil;

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

    // 메시지 객체를 JSON 문자열로 직렬화
    String messageJson = this.serializeMessageHistory(messageHistory);

    // Redis에 저장
    this.redisUtil.save(redisKey, messageJson, 3600);

    // 메시지 저장 결과 반환
    return new ChatMessageResponse(
        messageRequest.chatroomId(),
        messageRequest.senderId(),
        messageRequest.content(),
        LocalDateTime.parse(createdAt, DATE_TIME_FORMATTER));
  }
  
  private String serializeMessageHistory(ChatMessageHistory messageHistory) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(messageHistory);
    } catch (JsonProcessingException e) {
      throw new CustomException(ErrorCode.JSON_MAPPING_FAILURE);
    }
  }

}
