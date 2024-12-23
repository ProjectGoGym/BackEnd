package com.gogym.chat.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.schedule.ChatMessageBatchScheduler;
import com.gogym.chat.type.MessageType;
import com.gogym.exception.CustomException;
import com.gogym.util.JsonUtil;
import com.gogym.util.RedisService;

@ExtendWith(MockitoExtension.class)
class ChatMessageBatchSchedulerTest {

  @Mock
  private RedisService redisService;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private ListOperations<String, Object> listOperations;

  @InjectMocks
  private ChatMessageBatchScheduler chatMessageBatchScheduler;

  @Test
  public void 메시지를_Redis에서_읽어_DB에_저장하고_삭제() {
    // Given
    Long chatroomId = 1L;
    String redisKey = "chatroom:messages:1";
    RedisChatMessage chatMessage = new RedisChatMessage(
        "안녕하세요!",
        123L,
        MessageType.TEXT_ONLY,
        LocalDateTime.of(2024, 12, 25, 12, 0)
    );
    String redisChatMessageJson = JsonUtil.serialize(chatMessage);
    
    ChatRoom mockChatRoom = mock(ChatRoom.class);

    // RedisTemplate Mock 설정
    when(this.redisTemplate.keys("chatroom:messages:*")).thenReturn(Set.of(redisKey));

    // RedisService Mock 설정
    when(this.redisService.lrange(redisKey, 0, -1)).thenReturn(List.of(redisChatMessageJson));

    // ChatRoomRepository Mock 설정
    when(this.chatRoomRepository.findById(chatroomId)).thenReturn(Optional.of(mockChatRoom));

    // When
    this.chatMessageBatchScheduler.batchMessagesToDatabase();

    // Then
    ArgumentCaptor<ChatMessage> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
    verify(this.chatMessageRepository, times(1)).save(chatMessageCaptor.capture());

    ChatMessage savedMessage = chatMessageCaptor.getValue();
    assertEquals("안녕하세요!", savedMessage.getContent());
    assertEquals(123L, savedMessage.getSenderId());
    assertEquals(mockChatRoom, savedMessage.getChatRoom());
    assertEquals(MessageType.TEXT_ONLY, savedMessage.getMessageType());

    verify(this.redisService, times(1)).delete(redisKey);
  }

  @Test
  public void 채팅방이_존재하지_않는_경우_CustomException_발생() {
    // Given
    Long chatroomId = 1L;
    String redisKey = "chatroom:messages:1";
    String validMessageJson = "{\"content\":\"안녕하세요\",\"senderId\":123,\"messageType\":\"TEXT_ONLY\",\"createdAt\":\"2024-12-03T12:00:00\"}";
    
    // RedisTemplate Mock 설정
    when(this.redisTemplate.keys("chatroom:messages:*")).thenReturn(Set.of(redisKey));

    // RedisService Mock 설정
    when(this.redisService.lrange(redisKey, 0, -1)).thenReturn(List.of(validMessageJson));

    // ChatRoomRepository Mock 설정
    when(this.chatRoomRepository.findById(chatroomId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(CustomException.class, () -> this.chatMessageBatchScheduler.batchMessagesToDatabase());
  }

  @Test
  public void Redis에_메시지가_없는_경우_아무_작업도_하지_않음() {
    // Given
    String redisKey = "chatroom:messages:1";

    // RedisTemplate Mock 설정
    when(this.redisTemplate.keys("chatroom:messages:*")).thenReturn(Set.of(redisKey));

    // RedisService Mock 설정 (Redis에 메시지가 없는 상황)
    when(this.redisService.lrange(redisKey, 0, -1)).thenReturn(List.of()); // 빈 리스트 반환

    // When
    assertDoesNotThrow(() -> this.chatMessageBatchScheduler.batchMessagesToDatabase());

    // Then
    verify(this.chatRoomRepository, times(0)).findById(anyLong());
    verify(this.chatMessageRepository, times(0)).save(any(ChatMessage.class));
    verify(this.redisService, times(0)).delete(redisKey);
  }

}
