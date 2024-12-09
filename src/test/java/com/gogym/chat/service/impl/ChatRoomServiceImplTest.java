package com.gogym.chat.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
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
import org.springframework.data.domain.Pageable;
import com.gogym.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.gogym.chat.dto.ChatRoomDto.LeaveRequest;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageReadRepository;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.common.entity.BaseEntity;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.post.entity.Post;
import com.gogym.post.service.PostService;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceImplTest {

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private ChatMessageReadRepository chatMessageReadRepository;

  @Mock
  private MemberService memberService;

  @Mock
  private PostService postService;

  @Mock
  private ChatRedisServiceImpl chatRedisService;

  @InjectMocks
  private ChatRoomServiceImpl chatRoomService;

  private Member postAuthor;
  private Member requestor;
  private Post post;
  private ChatRoom chatRoom;
  private ChatMessage chatMessage;

  @BeforeEach
  void setUp() {
    this.postAuthor = Member.builder()
        .id(1L)
        .nickname("PostAuthor")
        .build();

    this.requestor = Member.builder()
        .id(2L)
        .nickname("Requestor")
        .build();
    
    this.post = Post.builder()
        .member(this.postAuthor)
        .build();
    
    this.chatRoom = ChatRoom.builder()
        .post(this.post)
        .requestor(this.requestor)
        .postAuthorActive(true)
        .requestorActive(true)
        .isDeleted(false)
        .build();
    
    this.chatMessage = ChatMessage.builder()
        .content("test message content")
        .senderId(this.postAuthor.getId())
        .chatRoom(this.chatRoom)
        .build();
  }

  @Test
  void 채팅방_생성_성공() {
    // Given
    Long postId = 1L;
    Post mockPost = null;

    when(this.postService.getPostAuthor(postId)).thenReturn(this.postAuthor);
    when(this.postService.findById(postId)).thenReturn(mockPost);

    // 이미 존재하는 채팅방 여부 확인
    when(this.chatRoomRepository.existsByPostIdAndRequestorId(postId, this.postAuthor.getId())).thenReturn(false);

    // ChatRoom 생성 후 저장된 상태로 반환
    when(this.chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
      ChatRoom savedChatRoom = invocation.getArgument(0);
      Field idField = ChatRoom.class.getSuperclass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(savedChatRoom, 1L);
      return savedChatRoom;
    });

    // When
    ChatRoomResponse response = this.chatRoomService.createChatRoom(this.postAuthor.getId(), postId);

    // Then
    assertNotNull(response);
    assertEquals(1L, response.chatRoomId());
    verify(this.chatRoomRepository).save(any(ChatRoom.class));
  }

  @Test
  void 채팅방_생성_실패_이미_존재() {
    // Given
    Long postId = 1L;

    when(this.chatRoomRepository.existsByPostIdAndRequestorId(postId, this.postAuthor.getId())).thenReturn(true);

    // When
    CustomException exception = assertThrows(
        CustomException.class,
        () -> this.chatRoomService.createChatRoom(this.postAuthor.getId(), postId));

    // Then
    assertEquals(ErrorCode.CHATROOM_ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  void 채팅방_목록_조회_성공() throws Exception {
    // Given
    Long chatRoomId = 1L;
    Long memberId = 1L;

    Field idField = BaseEntity.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(this.chatRoom, 1L);

    Page<ChatRoom> mockPage = new PageImpl<>(List.of(this.chatRoom));
    when(this.chatRoomRepository.findByPostMemberIdOrRequestorIdAndIsDeletedFalse(
        eq(memberId),
        eq(memberId),
        any(Pageable.class))).thenReturn(mockPage);

    String redisMessageJson = "{\"content\":\"test message\",\"senderId\":1,\"createdAt\":\"2024-12-10 12:00:00\"}";
    when(this.chatRedisService.getMessages(chatRoomId)).thenReturn(List.of(redisMessageJson));
    when(this.chatMessageReadRepository.countUnreadMessages(eq(chatRoomId), eq(memberId))).thenReturn(5);

    // When
    List<ChatRoomResponse> responses = this.chatRoomService.getChatRooms(memberId, 0, 10);

    // Then
    assertNotNull(responses);
    assertEquals(1, responses.size());

    ChatRoomResponse response = responses.get(0);
    
    assertEquals(chatRoomId, response.chatRoomId());
    assertEquals("test message", response.lastMessage());
    assertEquals(5, response.unreadMessageCount());
    
    verify(this.chatRoomRepository).findByPostMemberIdOrRequestorIdAndIsDeletedFalse(
        eq(memberId),
        eq(memberId),
        any(Pageable.class));
    verify(this.chatRedisService).getMessages(chatRoomId);
    verify(this.chatMessageReadRepository).countUnreadMessages(chatRoomId, memberId);
  }

  @Test
  void 채팅방_나가기_성공() {
    // Given
    Long chatRoomId = 1L;
    LeaveRequest request = new LeaveRequest(1L);

    String redisMessageJson = "{\"content\":\"test message\",\"senderId\":1,\"createdAt\":\"2024-12-10T12:00:00\"}";
    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(this.chatRoom));
    when(this.chatRedisService.getMessages(chatRoomId)).thenReturn(List.of(redisMessageJson));
    when(this.chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoomId)).thenReturn(Optional.of(this.chatMessage));

    // When
    assertDoesNotThrow(() -> this.chatRoomService.leaveChatRoom(this.postAuthor.getId(), chatRoomId, request));

    // Then
    verify(this.chatMessageReadRepository).save(any());
  }

  @Test
  void 채팅방_나가기_실패_채팅방_없음() {
    // Given
    Long chatRoomId = 1L;
    LeaveRequest request = new LeaveRequest(1L);

    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

    // When
    CustomException exception = assertThrows(
        CustomException.class,
        () -> this.chatRoomService.leaveChatRoom(this.postAuthor.getId(), chatRoomId, request));

    // Then
    assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void 채팅방_삭제_양쪽_사용자가_모두_나간_경우_isDeleted_변경() {
    // Given
    Long chatRoomId = 1L;
    ChatRoom testChatRoom = ChatRoom.builder()
        .post(this.post)
        .requestor(this.requestor)
        .postAuthorActive(false)
        .requestorActive(false)
        .isDeleted(false)
        .build();

    String redisMessageJson = "{\"content\":\"test message\",\"senderId\":1,\"createdAt\":\"2024-12-10T12:00:00\"}";
    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(testChatRoom));
    when(this.chatRedisService.getMessages(chatRoomId))
        .thenReturn(List.of(redisMessageJson)) // 첫 번째 호출에서는 Redis에 메시지 존재
        .thenReturn(List.of()); // 두 번째 호출에서는 Redis에 메시지 없음

    // When
    assertDoesNotThrow(() -> this.chatRoomService.deleteChatRoom(this.postAuthor.getId(), chatRoomId));
    assertDoesNotThrow(() -> this.chatRoomService.deleteChatRoom(this.requestor.getId(), chatRoomId));

    // Then
    assertTrue(testChatRoom.getIsDeleted());
    verify(this.chatRedisService, times(1)).deleteMessages(chatRoomId);
  }

  @Test
  void 채팅방_삭제_한쪽_사용자만_나간_경우_isDeleted_유지() {
    // Given
    Long chatRoomId = 1L;
    ChatRoom testChatRoom = ChatRoom.builder()
        .post(this.post)
        .requestor(this.requestor)
        .postAuthorActive(false) // 게시물 작성자만 채팅방을 나간 경우
        .requestorActive(true)
        .isDeleted(false)
        .build();

    String redisMessageJson = "{\"content\":\"test message\",\"senderId\":1,\"createdAt\":\"2024-12-10T12:00:00\"}";
    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(testChatRoom));
    when(this.chatRedisService.getMessages(chatRoomId)).thenReturn(List.of(redisMessageJson));

    // When
    assertDoesNotThrow(() -> this.chatRoomService.deleteChatRoom(this.postAuthor.getId(), chatRoomId));

    // Then
    assertFalse(testChatRoom.getIsDeleted());
  }

  @Test
  void 채팅방_삭제_실패_채팅방_없음() {
    // Given
    Long chatRoomId = 1L;

    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

    // When
    CustomException exception = assertThrows(
        CustomException.class,
        () -> this.chatRoomService.deleteChatRoom(this.postAuthor.getId(), chatRoomId));

    // Then
    assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
  }

}
