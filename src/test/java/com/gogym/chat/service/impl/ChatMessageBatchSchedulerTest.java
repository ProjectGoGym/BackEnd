package com.gogym.chat.service.impl;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.schedule.ChatMessageBatchScheduler;

@ExtendWith(MockitoExtension.class)
class ChatMessageBatchSchedulerTest {
  
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
  public void 메시지를_Redis에_저장하고_삭제() {
    // Given
    String redisKey = "chatroom:messages:1";
    Long chatroomId = 1L;

    String content = "안녕하세요!";
    Long senderId = 123L;
    String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    ChatRoom mockChatRoom = mock(ChatRoom.class);
    ChatMessageHistory messageHistory = new ChatMessageHistory(
        content,
        senderId,
        createdAt);

    when(this.redisTemplate.keys("chatroom:messages:*")).thenReturn(Set.of(redisKey));
    when(this.redisTemplate.opsForList()).thenReturn(this.listOperations);
    when(this.redisTemplate.opsForList().range(redisKey, 0, -1)).thenReturn(List.of(messageHistory));
    when(this.chatRoomRepository.findById(chatroomId)).thenReturn(Optional.of(mockChatRoom));

    // When
    this.chatMessageBatchScheduler.batchMessagesToDatabase();

    // Then
    verify(this.chatMessageRepository, times(1)).saveAll(anyList());
    verify(this.redisTemplate, times(1)).delete(redisKey);
  }
  
}
