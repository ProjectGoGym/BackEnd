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
import com.gogym.chat.dto.ChatMessageDto.SafePaymentMessageResponse;
import com.gogym.chat.dto.ChatMessageDto.SafePaymentRedisMessage;
import com.gogym.chat.dto.base.MessageRequest;
import com.gogym.chat.dto.base.MessageResponse;
import com.gogym.chat.dto.base.RedisMessage;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatMessageQueryService;
import com.gogym.chat.service.ChatMessageService;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.service.ChatRoomQueryService;
import com.gogym.chat.type.MessageType;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import com.gogym.gympay.event.SendMessageEvent;
import com.gogym.post.dto.PostSummaryDto;
import com.gogym.post.entity.Post;
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
  
  @Override
  public ChatRoomMessagesResponse getChatRoomMessagesAndPostInfo(Long memberId, Long chatRoomId, Pageable pageable) {
    // 회원이 해당 채팅방에 속해 있는지 확인
    if (!this.chatRoomQueryService.isMemberInChatRoom(chatRoomId, memberId)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // Redis와 DB에서 메시지를 조회 및 변환
    Page<MessageResponse> messagePage = this.getCombinedMessages(chatRoomId, pageable);

    // 채팅방에 연결된 게시물의 요약 정보 조회
    PostSummaryDto postSummaryDto = this.getConnectedPost(chatRoomId);
    
    // leaveAt 조회
    LocalDateTime leaveAt = this.chatRoomRepository.findWithLeaveAtById(chatRoomId)
        .map(chatRoom -> chatRoom.getLeaveAt(memberId))
        .orElse(null);
    
    // ChatRoomMessagesResponse 반환
    return new ChatRoomMessagesResponse(messagePage, postSummaryDto, leaveAt);
  }
  
  /**
   * Redis와 DB에서 메시지를 조회 후 병합하여 페이징 처리.
   * 
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return 병합된 {@link MessageResponse} 리스트
   */
  private Page<MessageResponse> getCombinedMessages(Long chatRoomId, Pageable pageable) {
    // Redis에서 메시지 조회 및 정렬
    List<MessageResponse> redisMessages = this.getRedisMessages(chatRoomId).stream()
        .sorted(Comparator.comparing(MessageResponse::createdAt).reversed())
        .toList();
    
    // DB에서 페이징된 메시지 조회
    List<MessageResponse> dbMessages = this.getDbMessages(chatRoomId, Pageable.unpaged());
    
    // DB 메시지와 Redis 메시지를 병합한 후 내림차순으로 정렬
    List<MessageResponse> combinedMessages = Stream.concat(dbMessages.stream(), redisMessages.stream())
        .sorted(Comparator.comparing(MessageResponse::createdAt).reversed())
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
   * @return {@link MessageResponse} 리스트
   */
  private List<MessageResponse> getRedisMessages(Long chatRoomId) {
    return this.chatRedisService.getMessages(chatRoomId).stream()
        .map(messageJson -> JsonUtil.deserialize(messageJson, RedisMessage.class))
        .filter(Objects::nonNull)
        .map(redisMessage -> {
          if (redisMessage instanceof SafePaymentRedisMessage safePaymentMessage) {
            return (MessageResponse) new SafePaymentMessageResponse(
                chatRoomId,
                safePaymentMessage.senderId(),
                safePaymentMessage.content(),
                safePaymentMessage.messageType(),
                safePaymentMessage.createdAt(),
                safePaymentMessage.safePaymentId(),
                safePaymentMessage.safePaymentStatus());
          } else if (redisMessage instanceof RedisChatMessage chatMessage) {
            return (MessageResponse) new ChatMessageResponse(
                chatRoomId,
                chatMessage.senderId(),
                chatMessage.content(),
                chatMessage.messageType(),
                chatMessage.createdAt());
          } else {
            throw new CustomException(ErrorCode.REQUEST_VALIDATION_FAIL);
          }
        }).toList();
  }

  
  /**
   * DB에서 메시지를 조회 후 변환.
   * 
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return {@link MessageResponse} 리스트
   */
  private List<MessageResponse> getDbMessages(Long chatRoomId, Pageable pageable) {
    return this.chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable).stream()
        .map(dbMessage -> {
          if (dbMessage.getMessageType().toString().startsWith("SYSTEM_SAFE_PAYMENT")) {
            return (MessageResponse) new SafePaymentMessageResponse(
                chatRoomId,
                dbMessage.getSenderId(),
                dbMessage.getContent(),
                dbMessage.getMessageType(),
                dbMessage.getCreatedAt(),
                dbMessage.getSafePaymentId(),
                dbMessage.getSafePaymentStatus());
          } else {
            return (MessageResponse) new ChatMessageResponse(
                chatRoomId,
                dbMessage.getSenderId(),
                dbMessage.getContent(),
                dbMessage.getMessageType(),
                dbMessage.getCreatedAt());
          }
        }).toList();
  }
  
  /**
   * 특정 채팅방에 연결된 게시물의 요약된 정보들을 조회.
   * 
   * @param chatRoomId 채팅방 ID
   * @return {@link PostSummaryDto} 채팅방에 연결된 게시물 정보 요약 DTO
   */
  private PostSummaryDto getConnectedPost(Long chatRoomId) {
    // 채팅방에 연결된 게시물 조회
    Post post = this.chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND)).getPost();
    
    // PostSummaryDto 변환 후 반환
    return new PostSummaryDto(
        post.getId(),
        post.getTitle(),
        post.getAmount(),
        post.getStatus());
  }
  
  @EventListener
  public void handleChatMessageEvent(SendMessageEvent event) {
    this.sendMessage(event, event.senderId());
  }
  
  @Override
  public void sendMessage(MessageRequest messageRequest, Long memberId) {
    // Redis에 저장 및 브로드캐스팅에 필요한 객체 선언
    MessageResponse savedMessage;
    
    if (messageRequest instanceof SendMessageEvent event) {
      // SendMessageEvent일 경우의 처리
      savedMessage = this.chatRedisService.saveMessageToRedis(
          new ChatMessageRequest(event.chatRoomId(), event.content()),
          event.senderId(),
          event.messageType(),
          event.safePaymentId(),
          event.safePaymentstatus()
      );
      this.broadcastMessage(
          event.chatRoomId(),
          savedMessage,
          event.messageType(),
          event.safePaymentId(),
          event.safePaymentstatus()
      );
    } else {
      // ChatMessageRequest일 경우의 처리
      savedMessage = this.chatRedisService.saveMessageToRedis(
          new ChatMessageRequest(messageRequest.chatRoomId(), messageRequest.content()),
          memberId,
          MessageType.TEXT_ONLY,
          null,
          null
      );
      this.broadcastMessage(
          messageRequest.chatRoomId(),
          savedMessage
      );
    }
  }

  private void broadcastMessage(
      Long chatRoomId,
      MessageResponse message) {
    this.messagingTemplate.convertAndSend(
        TOPIC_CHATROOM_PREFIX + chatRoomId,
        message
    );
  }
  
  private void broadcastMessage(
      Long chatRoomId,
      MessageResponse message,
      MessageType messageType,
      Long safePaymentId,
      SafePaymentStatus safePaymentStatus) {
    this.messagingTemplate.convertAndSend(
        TOPIC_CHATROOM_PREFIX + chatRoomId,
        new SendMessageEvent(
            message.chatRoomId(),
            message.senderId(),
            message.content(),
            messageType,
            safePaymentId,
            safePaymentStatus
        )
    );
  }
  
}
