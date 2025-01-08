package com.gogym.chat.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.gogym.chat.dto.ChatRoomDto.LeaveRequest;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.event.ChatRoomEvent;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.type.MessageType;
import com.gogym.common.entity.BaseEntity;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.post.entity.Post;
import com.gogym.post.service.PostQueryService;
import com.gogym.post.type.PostStatus;
import com.gogym.util.JsonUtil;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceImplTest {

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private MemberService memberService;
  
  @Mock
  private PostQueryService postQueryService;
  
  @Mock
  private ChatRedisServiceImpl chatRedisService;
  
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private ChatRoomServiceImpl chatRoomService;

  private Member postAuthor;
  private Member requestor;
  private Post post;
  private ChatRoom chatRoom;

  @BeforeEach
  void setUp() {
    this.postAuthor = Member.builder()
        .nickname("PostAuthor")
        .build();

    this.requestor = Member.builder()
        .nickname("Requestor")
        .build();
    
    this.post = Post.builder()
        .author(this.postAuthor)
        .build();
    
    this.chatRoom = ChatRoom.builder()
        .post(this.post)
        .requestor(this.requestor)
        .postAuthorActive(true)
        .requestorActive(true)
        .isDeleted(false)
        .build();
  }

  @Test
  void 채팅방_생성_성공() throws Exception {
    // Given
    Long postId = 1L;
    Long requestorId = this.requestor.getId();
    String requestorNickname = this.requestor.getNickname();
    
    Post mockPost = Post.builder()
        .status(PostStatus.PENDING)
        .author(this.postAuthor)
        .build();

    when(this.postQueryService.getPostAuthor(postId)).thenReturn(this.postAuthor);
    when(this.postQueryService.findById(postId)).thenReturn(mockPost);

    // 이미 존재하는 채팅방 여부 확인
    when(this.chatRoomRepository.existsByPostIdAndRequestorId(postId, this.postAuthor.getId())).thenReturn(false);

    // ChatRoom 생성 후 저장된 상태로 반환
    when(this.chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
      ChatRoom savedChatRoom = invocation.getArgument(0);
      
      Field requestorField = ChatRoom.class.getDeclaredField("requestor");
      requestorField.setAccessible(true);
      requestorField.set(savedChatRoom, this.requestor);
      
      Field idField = ChatRoom.class.getSuperclass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(savedChatRoom, 1L);
      
      return savedChatRoom;
    });
    
    doNothing().when(this.eventPublisher).publishEvent(any(ChatRoomEvent.class));

    // When
    ChatRoomResponse response = this.chatRoomService.createChatRoom(this.postAuthor.getId(), postId);

    // Then
    assertNotNull(response);
    assertEquals(1L, response.chatRoomId());
    verify(this.chatRoomRepository).save(any(ChatRoom.class));
    
    ArgumentCaptor<ChatRoomEvent> eventCaptor = ArgumentCaptor.forClass(ChatRoomEvent.class);
    verify(this.eventPublisher).publishEvent(eventCaptor.capture());
    
    ChatRoomEvent capturedEvent = eventCaptor.getValue();
    assertEquals(1L, capturedEvent.getChatRoomId());
    assertEquals(requestorId, capturedEvent.getRequestorId());
    assertEquals(requestorNickname, capturedEvent.getRequestorNickname());
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
    idField.set(this.postAuthor, 1L);
    idField.set(this.requestor, 2L);
    idField.set(this.chatRoom, chatRoomId);
    
    Page<ChatRoom> mockPage = new PageImpl<>(List.of(this.chatRoom));
    when(this.chatRoomRepository.findChatRoomsSortedByLastMessage(
        eq(memberId),
        eq(memberId),
        any(Pageable.class))).thenReturn(mockPage);

    // Redis 메시지 Mock 설정
    RedisChatMessage chatMessage = new RedisChatMessage(
        "안녕하세요!",
        123L,
        MessageType.TEXT_ONLY,
        LocalDateTime.of(2024, 12, 25, 12, 0)
    );
    String redisChatMessageJson = JsonUtil.serialize(chatMessage);
    when(this.chatRedisService.getMessages(chatRoomId)).thenReturn(List.of(redisChatMessageJson));

    // leaveAt 설정
    LocalDateTime leaveAt = LocalDateTime.now().minusHours(1);
    this.chatRoom.setLeaveAt(memberId, leaveAt);

    // When
    Page<ChatRoomResponse> responses = this.chatRoomService.getChatRooms(memberId, PageRequest.of(0, 10));

    // Then
    assertNotNull(responses);
    assertEquals(1, responses.getContent().size());

    ChatRoomResponse response = responses.getContent().get(0);
    assertEquals(chatRoomId, response.chatRoomId());
    assertEquals("안녕하세요!", response.lastMessage());
  }

  @Test
  void 채팅방_나가기_성공() {
    // Given
    Long memberId = 1L;
    Long chatRoomId = 1L;
    LeaveRequest request = new LeaveRequest(LocalDateTime.now().minusMinutes(5));
    
    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(this.chatRoom));
    when(this.chatRoomService.isMemberInChatRoom(chatRoomId, memberId)).thenReturn(true);
    
    // Redis 메시지 Mock 설정
    RedisChatMessage chatMessage = new RedisChatMessage(
        "안녕하세요!",
        123L,
        MessageType.TEXT_ONLY,
        LocalDateTime.of(2024, 12, 25, 12, 0)
    );
    String redisChatMessageJson = JsonUtil.serialize(chatMessage);
    when(this.chatRedisService.getMessages(chatRoomId)).thenReturn(List.of(redisChatMessageJson));
    
    // When
    assertDoesNotThrow(() -> this.chatRoomService.leaveChatRoom(memberId, chatRoomId, request));
    
    // Then
    verify(this.chatRedisService).getMessages(chatRoomId);
    assertEquals(request.leaveAt(), this.chatRoom.getLeaveAt(memberId));
  }

  @Test
  void 채팅방_나가기_실패_채팅방_없음() {
    // Given
    Long chatRoomId = 1L;
    LeaveRequest request = new LeaveRequest(LocalDateTime.now().minusMinutes(5));
    
    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());
    
    // When
    CustomException exception = assertThrows(
        CustomException.class,
        () -> this.chatRoomService.leaveChatRoom(this.postAuthor.getId(), chatRoomId, request));
    
    // Then
    assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void 채팅방_삭제_양쪽_사용자가_모두_나간_경우_isDeleted_변경() throws Exception {
    // Given
    Long chatRoomId = 1L;
    ChatRoom testChatRoom = ChatRoom.builder()
        .post(this.post)
        .requestor(this.requestor)
        .postAuthorActive(false)
        .requestorActive(false)
        .isDeleted(false)
        .build();
    
    Field idField = BaseEntity.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(this.postAuthor, 1L);
    idField.set(this.requestor, 2L);
    idField.set(this.chatRoom, chatRoomId);
    
    when(this.chatRoomService.isMemberInChatRoom(chatRoomId, this.postAuthor.getId())).thenReturn(true);
    when(this.chatRoomService.isMemberInChatRoom(chatRoomId, this.requestor.getId())).thenReturn(true);
    
    RedisChatMessage chatMessage = new RedisChatMessage(
        "안녕하세요!",
        123L,
        MessageType.TEXT_ONLY,
        LocalDateTime.of(2024, 12, 25, 12, 0)
    );
    String redisChatMessageJson = JsonUtil.serialize(chatMessage);
    when(this.chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(testChatRoom));
    when(this.chatRedisService.getMessages(chatRoomId))
        .thenReturn(List.of(redisChatMessageJson)) // 첫 번째 호출에서는 Redis에 메시지 존재
        .thenReturn(List.of()); // 두 번째 호출에서는 Redis에 메시지 없음

    // When
    assertDoesNotThrow(() -> this.chatRoomService.deleteChatRoom(this.postAuthor.getId(), chatRoomId));
    assertDoesNotThrow(() -> this.chatRoomService.deleteChatRoom(this.requestor.getId(), chatRoomId));

    // Then
    assertTrue(testChatRoom.getIsDeleted());
    verify(this.chatRedisService, times(1)).deleteMessages(chatRoomId);
  }

  @Test
  void 채팅방_삭제_한쪽_사용자만_나간_경우_isDeleted_유지() throws Exception {
    // Given
    Long memberId = 1L;
    Long chatRoomId = 1L;
    ChatRoom testChatRoom = ChatRoom.builder()
        .post(this.post)
        .requestor(this.requestor)
        .postAuthorActive(false) // 게시물 작성자만 채팅방을 나간 경우
        .requestorActive(true)
        .isDeleted(false)
        .build();
    
    Field idField = BaseEntity.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(this.postAuthor, 1L);
    idField.set(this.requestor, 2L);
    idField.set(this.chatRoom, chatRoomId);
    
    when(this.chatRoomService.isMemberInChatRoom(chatRoomId, memberId)).thenReturn(true);
    
    RedisChatMessage chatMessage = new RedisChatMessage(
        "테스트메세지입니다.",
        123L,
        MessageType.TEXT_ONLY,
        LocalDateTime.of(2024, 12, 25, 12, 0)
    );
    String redisMessageJson = JsonUtil.serialize(chatMessage);
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
