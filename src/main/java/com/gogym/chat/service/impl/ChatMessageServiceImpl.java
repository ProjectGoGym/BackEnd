package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.gogym.chat.dto.ChatMessageDto.ChatRoomMessagesResponse;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.dto.MessageRequest;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatMessageQueryService;
import com.gogym.chat.service.ChatMessageService;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.service.ChatRoomQueryService;
import com.gogym.chat.type.MessageType;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.event.SendMessageEvent;
import com.gogym.post.service.PostService;
import com.gogym.post.type.PostStatus;
import com.gogym.util.JsonUtil;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageQueryService, ChatMessageService {
  
  private static final String TOPIC_CHATROOM_PREFIX = "/topic/chatroom/";
  
  private final SimpMessagingTemplate messagingTemplate;
  
  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  
  private final ChatRedisService chatRedisService;
  private final ChatRoomQueryService chatRoomQueryService;
  private final PostService postService;
  
  @Override
  public ChatRoomMessagesResponse getMessagesWithPostStatus(Long memberId, Long chatRoomId, Pageable pageable) {
    // 회원이 해당 채팅방에 속해 있는지 확인
    if (!this.chatRoomQueryService.isMemberInChatRoom(chatRoomId, memberId)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // Redis와 DB에서 메시지를 조회 및 변환
    Page<ChatMessageResponse> messagePage = this.getCombinedMessages(chatRoomId, pageable);

    // 채팅방에 연결된 게시물 상태값을 조회
    PostStatus postStatus = this.getPostStatus(chatRoomId);
    
    // leaveAt 조회
    LocalDateTime leaveAt = this.chatRoomRepository.findWithLeaveAtById(chatRoomId)
        .map(chatRoom -> chatRoom.getLeaveAt(memberId))
        .orElse(null);
    
    // ChatRoomMessagesResponse 반환
    return new ChatRoomMessagesResponse(messagePage, postStatus, leaveAt);
  }
  
  /**
   * Redis와 DB에서 메시지를 조회 후 병합하여 페이징 처리.
   * 
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return 병합된 {@link ChatMessageResponse} 리스트
   */
  private Page<ChatMessageResponse> getCombinedMessages(Long chatRoomId, Pageable pageable) {
    // Redis에서 메시지 조회 및 정렬
    List<ChatMessageResponse> redisMessages = this.getRedisMessages(chatRoomId).stream()
        .sorted(Comparator.comparing(ChatMessageResponse::createdAt).reversed())
        .toList();
    
    // DB에서 페이징된 메시지 조회
    List<ChatMessageResponse> dbMessages = this.getDbMessages(chatRoomId, Pageable.unpaged());
    
    // DB 메시지와 Redis 메시지를 병합한 후 내림차순으로 정렬
    List<ChatMessageResponse> combinedMessages = Stream.concat(dbMessages.stream(), redisMessages.stream())
        .sorted(Comparator.comparing(ChatMessageResponse::createdAt).reversed())
        .toList();
    
    // Pageable 조건에 맞게 페이징 처리
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), combinedMessages.size());
    
    // 경계값 확인 및 반환
    return start >= combinedMessages.size()
        ? new PageImpl<>(List.of(), pageable, combinedMessages.size())
        : new PageImpl<>(combinedMessages.subList(start, end), pageable, combinedMessages.size());
  }
  
  /**
   * Redis에서 메시지를 조회 후 변환.
   * 
   * @param chatRoomId 채팅방 ID
   * @return {@link ChatMessageResponse} 리스트
   */
  private List<ChatMessageResponse> getRedisMessages(Long chatRoomId) {
    return this.chatRedisService.getMessages(chatRoomId).stream()
        .map(messageJson -> JsonUtil.deserialize(messageJson, RedisChatMessage.class))
        .filter(Objects::nonNull)
        .map(redisMessage -> new ChatMessageResponse(
            chatRoomId,
            redisMessage.senderId(),
            redisMessage.content(),
            redisMessage.messageType(),
            redisMessage.createdAt()
        )).toList();
  }
  
  /**
   * DB에서 메시지를 조회 후 변환.
   * 
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return {@link ChatMessageResponse} 리스트
   */
  private List<ChatMessageResponse> getDbMessages(Long chatRoomId, Pageable pageable) {
    return this.chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable).stream()
        .map(dbMessage -> new ChatMessageResponse(
            chatRoomId,
            dbMessage.getSenderId(),
            dbMessage.getContent(),
            dbMessage.getMessageType(),
            dbMessage.getCreatedAt()
        )).toList();
  }
  
  /**
   * 특정 채팅방에 연결된 게시물의 상태값을 조회.
   * 
   * @param chatRoomId 채팅방 ID
   * @return {@link PostStatus} 게시물 상태
   */
  private PostStatus getPostStatus(Long chatRoomId) {
    // 채팅방 ID를 기반으로 게시물 ID 조회
    Long postId = this.chatRoomRepository.findPostIdByChatRoomId(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    
    // 게시물 ID를 기반으로 상태값 조회
    return this.postService.getPostStatus(postId);
  }
  
  @EventListener
  public void handleChatMessageEvent(SendMessageEvent event) {
    this.sendMessage(event, event.senderId());
  }
  
  @Override
  public void sendMessage(MessageRequest messageRequest, Long memberId) {
    // Redis에 저장 및 브로드캐스팅에 필요한 객체 선언
    ChatMessageResponse savedMessage;
    
    if (messageRequest instanceof SendMessageEvent event) {
      // SendMessageEvent일 경우의 처리
      savedMessage = this.chatRedisService.saveMessageToRedis(
          new ChatMessageRequest(event.chatRoomId(), event.content()),
          event.senderId(),
          event.messageType()
      );
      this.broadcastMessage(event.chatRoomId(), savedMessage, event.messageType());
    } else {
      // ChatMessageRequest일 경우의 처리
      savedMessage = this.chatRedisService.saveMessageToRedis(
          new ChatMessageRequest(messageRequest.chatRoomId(), messageRequest.content()),
          memberId,
          MessageType.TEXT_ONLY
      );
      this.broadcastMessage(messageRequest.chatRoomId(), savedMessage);
    }
  }

  private void broadcastMessage(Long chatRoomId, ChatMessageResponse message) {
    this.messagingTemplate.convertAndSend(
        TOPIC_CHATROOM_PREFIX + chatRoomId,
        message
    );
  }
  
  private void broadcastMessage(Long chatRoomId, ChatMessageResponse message, MessageType messageType) {
    this.messagingTemplate.convertAndSend(
        TOPIC_CHATROOM_PREFIX + chatRoomId,
        new SendMessageEvent(
            message.chatRoomId(),
            message.senderId(),
            message.content(),
            messageType)
    );
  }
  
}
