package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.util.JsonUtil;
import com.gogym.util.RedisUtil;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRedisServiceImpl implements ChatRedisService {
  
  private final RedisUtil redisUtil;
  
  private final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";
  
  @Override
  public ChatMessageResponse saveMessageToRedis(ChatMessageRequest messageRequest, Long memberId) {
    // Redis Key 생성
    String redisKey = this.getRedisChatroomMessageKeyPrefix() + messageRequest.chatRoomId();

    // 현재 시간 설정
    LocalDateTime createdAt = LocalDateTime.now();
    
    // Redis에 저장할 메시지 객체 생성
    RedisChatMessage  messageHistory = new RedisChatMessage(
        messageRequest.content(),
        memberId,
        createdAt
    );

    // 메시지 객체를 JSON 문자열로 직렬화
    String messageJson = JsonUtil.serialize(messageHistory);

    // Redis에 저장
    this.redisUtil.rpush(redisKey, messageJson);

    // 메시지 저장 결과 반환
    return new ChatMessageResponse(
        messageRequest.chatRoomId(),
        memberId,
        messageRequest.content(),
        createdAt);
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
