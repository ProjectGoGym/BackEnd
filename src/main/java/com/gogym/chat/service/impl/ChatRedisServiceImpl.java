package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
  public ChatMessageResponse saveMessageToRedis(ChatMessageRequest messageRequest, Long memberId) {
    // Redis Key 생성
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + messageRequest.chatRoomId();

    // LocalDateTime을 String으로 변환
    String createdAt = LocalDateTime.now().format(DATE_TIME_FORMATTER);

    // Redis에 저장할 메시지 객체 생성
    ChatMessageHistory messageHistory = new ChatMessageHistory(
        messageRequest.content(),
        memberId,
        createdAt);

    // 메시지 객체를 JSON 문자열로 직렬화
    String messageJson = JsonUtil.serialize(messageHistory);

    // Redis에 저장
    this.redisUtil.rpush(redisKey, messageJson);

    // 메시지 저장 결과 반환
    return new ChatMessageResponse(
        messageRequest.chatRoomId(),
        memberId,
        messageRequest.content(),
        LocalDateTime.parse(createdAt, DATE_TIME_FORMATTER));
  }
  
  @Override
  public List<String> getMessages(Long chatRoomId) {
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + chatRoomId;
    return this.redisUtil.lrange(redisKey, 0, -1);
  }
  
  @Override
  public void deleteMessages(Long chatRoomId) {
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + chatRoomId;
    this.redisUtil.delete(redisKey);
  }

  @Override
  public String getRedisChatroomMessageKeyPrefix() {
    return this.REDIS_CHATROOM_MESSAGE_KEY;
  }

}
