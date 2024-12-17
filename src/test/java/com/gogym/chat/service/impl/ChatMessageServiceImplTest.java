package com.gogym.chat.service.impl;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.gogym.chat.dto.ChatMessageDto.ChatRoomMessagesResponse;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.post.service.PostService;
import com.gogym.post.type.PostStatus;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {
  
  @Mock
  private ChatMessageRepository chatMessageRepository;
  
  @Mock
  private ChatRoomRepository chatRoomRepository;
  
  @Mock
  private ChatRedisService chatRedisService;
  
  @Mock
  private ChatRoomService chatRoomService;
  
  @Mock
  private PostService postService;
  
  @InjectMocks
  private ChatMessageServiceImpl chatMessageService;
  
  private Pageable pageable;
  
  @BeforeEach
  void setup() {
    this.pageable = PageRequest.of(0, 6);
  }
  
  @Test
  void 채팅방_메시지와_게시물_상태_조회_성공() {
    // Given
    Long memberId = 1L;
    Long chatRoomId = 1L;
    Long postId = 1L;
    
    String redisMessageJson = "{\"content\":\"Redis메시지입니다.\",\"senderId\":123,\"createdAt\":\"2024-12-25T12:00:00\"}";
    when(this.chatRedisService.getMessages(chatRoomId)).thenReturn(List.of(redisMessageJson));
    
    ChatMessage dbMessage = mock(ChatMessage.class);
    when(dbMessage.getContent()).thenReturn("DB메시지입니다.");
    when(dbMessage.getSenderId()).thenReturn(2L);
    when(dbMessage.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 12, 25, 12, 0));
    Page<ChatMessage> dbMessages = new PageImpl<>(List.of(dbMessage));
    when(this.chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, Pageable.unpaged())).thenReturn(dbMessages);
    
    when(this.chatRoomService.isMemberInChatRoom(chatRoomId, memberId)).thenReturn(true);
    when(this.chatRoomRepository.findPostIdByChatRoomId(chatRoomId)).thenReturn(Optional.of(postId));
    when(this.postService.getPostStatusByPostId(postId)).thenReturn(PostStatus.POSTING);
    
    // When
    ChatRoomMessagesResponse response = this.chatMessageService.getMessagesWithPostStatus(memberId, chatRoomId, this.pageable);
    
    // Then
    assertNotNull(response);
    assertEquals(2, response.messages().getContent().size());
    assertEquals("DB메시지입니다.", response.messages().getContent().get(0).content());
    assertEquals("Redis메시지입니다.", response.messages().getContent().get(1).content());
    assertEquals(PostStatus.POSTING, response.postStatus());
    
    verify(this.chatRedisService).getMessages(chatRoomId);
    verify(this.chatMessageRepository).findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, Pageable.unpaged());
    verify(this.chatRoomRepository).findPostIdByChatRoomId(chatRoomId);
    verify(this.postService).getPostStatusByPostId(postId);
  }
  
  @Test
  void 채팅방_없음_예외() {
    // Given
    Long memberId = 1L;
    Long chatRoomId = 1L;
    
    when(this.chatRoomService.isMemberInChatRoom(chatRoomId, memberId)).thenReturn(true);
    
    when(this.chatRoomRepository.findPostIdByChatRoomId(chatRoomId)).thenReturn(Optional.empty());
    when(this.chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, Pageable.unpaged())).thenReturn(Page.empty());
    
    // When
    Throwable thrown = catchThrowable(
        () -> this.chatMessageService.getMessagesWithPostStatus(memberId, chatRoomId, this.pageable)
    );
    
    // Then
    assertNotNull(thrown);
    assertTrue(thrown instanceof CustomException);
    assertEquals(ErrorCode.POST_NOT_FOUND, ((CustomException) thrown).getErrorCode());
    
    verify(this.chatRoomRepository).findPostIdByChatRoomId(chatRoomId);
  }

  @Test
  void 게시물_상태_조회_실패_예외() {
    // Given
    Long memberId = 1L;
    Long chatRoomId = 1L;
    Long postId = 1L;
    
    when(this.chatRoomService.isMemberInChatRoom(chatRoomId, memberId)).thenReturn(true);
    
    when(this.chatRoomRepository.findPostIdByChatRoomId(chatRoomId)).thenReturn(Optional.of(postId));
    when(this.chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, Pageable.unpaged())).thenReturn(Page.empty());
    when(this.postService.getPostStatusByPostId(postId)).thenThrow(new CustomException(ErrorCode.POST_NOT_FOUND));
    
    // When
    Throwable thrown = catchThrowable(
        () -> this.chatMessageService.getMessagesWithPostStatus(memberId, chatRoomId, this.pageable)
    );
    
    // Then
    assertNotNull(thrown);
    assertTrue(thrown instanceof CustomException);
    assertEquals(ErrorCode.POST_NOT_FOUND, ((CustomException) thrown).getErrorCode());
    
    verify(this.chatRoomRepository).findPostIdByChatRoomId(chatRoomId);
    verify(this.postService).getPostStatusByPostId(postId);
  }
  
}
