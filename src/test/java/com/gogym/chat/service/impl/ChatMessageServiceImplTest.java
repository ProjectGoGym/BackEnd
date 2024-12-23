package com.gogym.chat.service.impl;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.dto.ChatMessageDto.ChatRoomMessagesResponse;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.service.ChatRoomQueryService;
import com.gogym.chat.type.MessageType;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.event.SendMessageEvent;
import com.gogym.post.entity.Post;
import com.gogym.post.service.PostQueryService;
import com.gogym.region.service.RegionService;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRedisService chatRedisService;

  @Mock
  private ChatRoomQueryService chatRoomQueryService;

  @Mock
  private PostQueryService postQueryService;

  @Mock
  private RegionService regionService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private ChatMessageServiceImpl chatMessageService;

  private Pageable pageable;

  @BeforeEach
  void setup() {
    this.pageable = PageRequest.of(0, 6);
  }

  @Test
  void 메시지와_게시물_조회_성공() {
    // Given
    Long memberId = 1L;
    Long chatRoomId = 1L;
    
    // Mock Post 객체 생성
    Post post = mock(Post.class);
    
    // Mock ChatRoom 객체 생성 및 동작 설정
    ChatRoom chatRoom = mock(ChatRoom.class);
    when(chatRoom.getPost()).thenReturn(post);
    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
    
    // Mock 권한 확인 메서드 설정
    when(this.chatRoomQueryService.isMemberInChatRoom(chatRoomId, memberId)).thenReturn(true);
    
    // Mock Redis 메시지 설정
    String redisMessageJson = 
        "{\"content\":\"Redis메시지입니다.\","
        + "\"senderId\":123,"
        + "\"messageType\":\"TEXT_ONLY\","
        + "\"createdAt\":\"2024-12-03T12:00:00\"}";
    when(this.chatRedisService.getMessages(chatRoomId)).thenReturn(List.of(redisMessageJson));
    
    // Mock DB 메시지 설정
    ChatMessage dbMessage = mock(ChatMessage.class);
    when(dbMessage.getContent()).thenReturn("DB메시지입니다.");
    when(dbMessage.getSenderId()).thenReturn(2L);
    when(dbMessage.getMessageType()).thenReturn(MessageType.TEXT_ONLY);
    when(dbMessage.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 25, 12, 0));
    Page<ChatMessage> dbMessages = new PageImpl<>(List.of(dbMessage));
    when(this.chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(
        chatRoomId,
        Pageable.unpaged())).thenReturn(dbMessages);
    
    // When
    ChatRoomMessagesResponse response = this.chatMessageService.getChatRoomMessagesAndPostInfo(memberId, chatRoomId, this.pageable);
    
    // Then
    assertNotNull(response);
    assertEquals(2, response.messages().getContent().size());
    assertEquals("DB메시지입니다.", response.messages().getContent().get(0).content());
    assertEquals("Redis메시지입니다.", response.messages().getContent().get(1).content());
    
    verify(this.chatRoomQueryService).isMemberInChatRoom(chatRoomId, memberId);
    verify(this.chatRedisService).getMessages(chatRoomId);
    verify(this.chatMessageRepository).findByChatRoomIdOrderByCreatedAtDesc(eq(chatRoomId), any(Pageable.class));
  }


  @Test
  void 이벤트_기반_메시지_전송_성공() {
    // Given
    Long chatRoomId = 1L;
    Long senderId = 1L;
    String content = "이벤트메시지입니다.";
    MessageType messageType = MessageType.SYSTEM_SAFE_PAYMENT_REQUEST;
    
    SendMessageEvent event = new SendMessageEvent(chatRoomId, senderId, content, messageType);
    ChatMessageResponse savedMessage = new ChatMessageResponse(chatRoomId, senderId, content, messageType, LocalDateTime.now());
    
    when(this.chatRedisService.saveMessageToRedis(
        any(ChatMessageRequest.class),
        eq(senderId),
        eq(messageType))).thenReturn(savedMessage);
    
    // When
    this.chatMessageService.handleChatMessageEvent(event);
    
    // Then
    verify(this.chatRedisService).saveMessageToRedis(any(ChatMessageRequest.class), eq(senderId), eq(messageType));
    verify(this.messagingTemplate).convertAndSend(eq("/topic/chatroom/" + chatRoomId), any(SendMessageEvent.class));
  }

  @Test
  void 일반_메시지_전송_성공() {
    // Given
    Long chatRoomId = 1L;
    Long senderId = 2L;
    String content = "일반메시지입니다.";
    MessageType messageType = MessageType.TEXT_ONLY;
    
    ChatMessageRequest messageRequest = new ChatMessageRequest(chatRoomId, content);
    ChatMessageResponse savedMessage = new ChatMessageResponse(chatRoomId, senderId, content, messageType, LocalDateTime.now());
    
    when(this.chatRedisService.saveMessageToRedis(messageRequest, senderId, messageType)).thenReturn(savedMessage);
    
    // When
    this.chatMessageService.sendMessage(messageRequest, senderId);
    
    // Then
    verify(this.chatRedisService).saveMessageToRedis(messageRequest, senderId, messageType);
    verify(this.messagingTemplate).convertAndSend("/topic/chatroom/" + chatRoomId, savedMessage);
  }

  @Test
  void 채팅방_참여하지_않은_사용자_예외_발생() {
    // Given
    Long memberId = 1L;
    Long chatRoomId = 1L;
    
    when(this.chatRoomQueryService.isMemberInChatRoom(chatRoomId, memberId)).thenReturn(false);
    
    // When
    Throwable thrown = catchThrowable(
        () -> this.chatMessageService.getChatRoomMessagesAndPostInfo(memberId, chatRoomId, this.pageable));
    
    // Then
    assertTrue(thrown instanceof CustomException);
    assertEquals(ErrorCode.FORBIDDEN, ((CustomException) thrown).getErrorCode());
  }
  
}
