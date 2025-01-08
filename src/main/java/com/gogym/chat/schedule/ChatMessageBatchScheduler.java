package com.gogym.chat.schedule;

import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.dto.ChatMessageDto.SafePaymentRedisMessage;
import com.gogym.chat.dto.base.RedisMessage;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.util.JsonUtil;
import com.gogym.util.RedisService;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class ChatMessageBatchScheduler {

  private static final String REDIS_CHATROOM_MESSAGE_KEY = "chatroom:messages:";

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisService redisService;

  /**
   * 일정 주기로 Redis에 저장된 메시지를 DB로 저장.
   * Redis에 저장된 메시지를 읽어서 DB에 저장한 뒤 Redis에서 해당 메시지를 삭제합니다.
   */
  @Scheduled(fixedRate = 60000)
  public void batchMessagesToDatabase() {
    Set<String> redisKeys = this.redisTemplate.keys(REDIS_CHATROOM_MESSAGE_KEY + "*");

    redisKeys.forEach(redisKey -> {
      // Redis에서 모든 메시지 가져오기
      List<String> messagesJson = this.redisService.lrange(redisKey, 0, -1);

      if (messagesJson == null || messagesJson.isEmpty()) {
        return;
      }

      // Redis Key에서 chatroomId 추출
      String chatroomIdStr = redisKey.replace(REDIS_CHATROOM_MESSAGE_KEY, "");
      Long chatroomId = Long.parseLong(chatroomIdStr);

      // 채팅방 정보 조회
      ChatRoom chatRoom = this.chatRoomRepository.findById(chatroomId)
          .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

      // 메시지 처리 및 저장
      this.processMessages(chatRoom, messagesJson);

      // Redis에서 해당 채팅방 메시지 삭제
      this.redisService.delete(redisKey);
    });
  }
  
  /**
   * Redis 메시지를 처리.
   * 
   * @param chatRoom 채팅방 엔티티
   * @param messagesJson Redis에서 가져온 메시지 JSON 리스트
   */
  private void processMessages(ChatRoom chatRoom, List<String> messagesJson) {
    messagesJson.forEach(messageJson -> {
      // JSON에서 messageType 필드 추출
      String messageType = JsonUtil.extractField(messageJson, "messageType");
      
      RedisMessage redisMessage;
      if (messageType.startsWith("SYSTEM_SAFE_PAYMENT")) {
        // 안전거래 메시지일 경우
        redisMessage = JsonUtil.deserialize(messageJson, SafePaymentRedisMessage.class);
        this.saveMessage(chatRoom, (SafePaymentRedisMessage) redisMessage);
      } else {
        // 일반 메시지일 경우
        redisMessage = JsonUtil.deserialize(messageJson, RedisChatMessage.class);
        this.saveMessage(chatRoom, (RedisChatMessage) redisMessage);
      }
    });
  }
  
  /**
   * 일반 메시지를 DB에 저장.
   * 
   * @param chatRoom 채팅방 엔티티
   * @param chatMessage 일반 메시지
   */
  private void saveMessage(ChatRoom chatRoom, RedisChatMessage chatMessage) {
    ChatMessage message = ChatMessage.builder()
        .chatRoom(chatRoom)
        .content(chatMessage.content())
        .senderId(chatMessage.senderId())
        .messageType(chatMessage.messageType())
        .build();
    this.chatMessageRepository.save(message);
  }
  
  /**
   * 안전결제 메시지를 DB에 저장.
   * 
   * @param chatRoom 채팅방 엔티티
   * @param safePaymentMessage 안전결제 메시지
   */
  private void saveMessage(ChatRoom chatRoom, SafePaymentRedisMessage safePaymentMessage) {
    ChatMessage chatMessage = ChatMessage.builder()
        .chatRoom(chatRoom)
        .content(safePaymentMessage.content())
        .senderId(safePaymentMessage.senderId())
        .messageType(safePaymentMessage.messageType())
        .safePaymentId(safePaymentMessage.safePaymentId())
        .safePaymentStatus(safePaymentMessage.safePaymentStatus())
        .build();
    this.chatMessageRepository.save(chatMessage);
  }
  
}
