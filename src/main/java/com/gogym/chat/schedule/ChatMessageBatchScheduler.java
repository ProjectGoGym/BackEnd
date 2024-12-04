package com.gogym.chat.schedule;

import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.util.JsonUtil;
import com.gogym.util.RedisUtil;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class ChatMessageBatchScheduler {

  private static final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisUtil redisUtil;

  /**
   * 일정 주기로 Redis에 저장된 메시지를 DB로 저장.
   * Redis에 저장된 메시지를 읽어서 DB에 저장한 뒤 Redis에서 해당 메시지를 삭제합니다.
   */
  @Scheduled(fixedRate = 60000)
  public void batchMessagesToDatabase() {
    Set<String> redisKeys = this.redisTemplate.keys(REDIS_CHATROOM_MESSAGE_KEY + "*");

    redisKeys.forEach(redisKey -> {
      // Redis에서 메시지 가져오기
      String messageJson = this.redisUtil.get(redisKey);
      if (messageJson == null || messageJson.isEmpty()) {
        return;
      }

      // Redis Key에서 chatroomId 추출
      String chatroomIdStr = redisKey.replace(REDIS_CHATROOM_MESSAGE_KEY, "");
      Long chatroomId = Long.parseLong(chatroomIdStr);

      // 채팅방 정보 조회
      ChatRoom chatRoom = this.chatRoomRepository.findById(chatroomId)
          .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

      // JSON 문자열을 ChatMessageHistory 객체로 역직렬화
      ChatMessageHistory messageHistory = JsonUtil.deserialize(messageJson, ChatMessageHistory.class);

      // ChatMessage 엔티티로 변환
      ChatMessage chatMessage = ChatMessage.builder()
          .chatRoom(chatRoom)
          .content(messageHistory.content())
          .senderId(messageHistory.senderId())
          .build();

      // DB에 저장
      this.chatMessageRepository.save(chatMessage);

      // Redis에서 메시지 삭제
      this.redisUtil.delete(redisKey);
    });
  }

}
