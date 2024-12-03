package com.gogym.chat.schedule;

import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class ChatMessageBatchScheduler {

  private static final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * 일정 주기로 Redis에 저장된 메시지를 DB로 저장.
   * Redis에 저장된 메시지를 읽어서 DB에 저장한 뒤 Redis에서 해당 메시지를 삭제합니다.
   */
  @Scheduled(fixedRate = 300000)
  public void batchMessagesToDatabase() {
    Set<String> redisKeys = this.redisTemplate.keys(REDIS_CHATROOM_MESSAGE_KEY + "*");

    redisKeys.forEach(redisKey -> {
      // Redis Key에서 chatroomId 추출
      String chatroomIdStr = redisKey.replace(REDIS_CHATROOM_MESSAGE_KEY, "");
      Long chatroomId = Long.parseLong(chatroomIdStr);

      // 채팅방 정보 조회
      ChatRoom chatRoom = this.chatRoomRepository.findById(chatroomId)
          .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

      // Redis 메시지 가져오기
      List<Object> messages = this.redisTemplate.opsForList().range(redisKey, 0, -1);
      if (messages == null || messages.isEmpty()) {
        return;
      }

      // Redis 메시지를 ChatMessage 엔티티로 변환
      List<ChatMessage> chatMessages = messages.stream()
          .filter(msg -> msg instanceof ChatMessageHistory)
          .map(msg -> {
            ChatMessageHistory history = (ChatMessageHistory) msg;
            return ChatMessage.builder()
                .chatRoom(chatRoom)
                .content(history.content())
                .senderId(history.senderId())
                .build();
          }).collect(Collectors.toList());

      // DB에 저장
      this.chatMessageRepository.saveAll(chatMessages);

      // Redis 메시지 삭제
      this.redisTemplate.delete(redisKey);
    });
  }
}
