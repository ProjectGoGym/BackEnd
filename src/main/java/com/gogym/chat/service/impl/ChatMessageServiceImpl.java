package com.gogym.chat.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.dto.ChatMessageDto.ChatRoomMessagesResponse;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatMessageService;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.post.service.PostService;
import com.gogym.post.type.PostStatus;
import com.gogym.util.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
  
  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  
  private final ChatRedisService chatRedisService;
  private final ChatRoomService chatRoomService;
  private final PostService postService;
  
  @Override
  public ChatRoomMessagesResponse getMessagesWithPostStatus(Long memberId, Long chatRoomId, Pageable pageable) {
    // 회원이 해당 채팅방에 속해 있는지 확인
    if (!this.chatRoomService.isMemberInChatRoom(chatRoomId, memberId)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // Redis와 DB에서 메시지를 조회 및 변환
    List<ChatMessageHistory> combinedMessages = this.getCombinedMessages(chatRoomId, pageable);

    // 병합된 메시지 목록을 페이징 처리
    Page<ChatMessageHistory> messagePage = this.paginateMessages(combinedMessages, pageable);

    // 채팅방에 연결된 게시물 상태값을 조회
    PostStatus postStatus = this.getPostStatus(chatRoomId);

    // ChatRoomMessagesResponse 반환
    return new ChatRoomMessagesResponse(messagePage, postStatus);
  }
  
  /**
   * Redis와 DB에서 메시지를 조회 후 병합.
   * 
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return 병합된 {@link ChatMessageHistory} 리스트
   */
  private List<ChatMessageHistory> getCombinedMessages(Long chatRoomId, Pageable pageable) {
    return Stream
        .concat(
            this.getRedisMessages(chatRoomId).stream(),
            this.getDbMessages(chatRoomId, pageable).stream())
        .sorted(Comparator.comparing(ChatMessageHistory::createdAt))
        .toList();
  }
  
  /**
   * Redis에서 메시지를 조회 후 변환.
   * 
   * @param chatRoomId 채팅방 ID
   * @return {@link ChatMessageHistory} 리스트
   */
  private List<ChatMessageHistory> getRedisMessages(Long chatRoomId) {
    return this.chatRedisService.getMessages(chatRoomId).stream()
        .map(messageJson -> JsonUtil.deserialize(messageJson, RedisChatMessage.class))
        .filter(Objects::nonNull)
        .map(redisMessage -> new ChatMessageHistory(
            redisMessage.content(),
            redisMessage.senderId(),
            redisMessage.createdAt()
        )).toList();
  }
  
  /**
   * DB에서 메시지를 조회 후 변환.
   * 
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return {@link ChatMessageHistory} 리스트
   */
  private List<ChatMessageHistory> getDbMessages(Long chatRoomId, Pageable pageable) {
    return this.chatMessageRepository.findByChatRoomId(chatRoomId, pageable).stream()
        .map(dbMessage -> new ChatMessageHistory(
            dbMessage.getContent(),
            dbMessage.getSenderId(),
            dbMessage.getCreatedAt()
        )).toList();
  }
  
  /**
   * 메시지 리스트를 페이징 처리.
   * 
   * @param combinedMessages 병합된 메시지 리스트
   * @param pageable 페이징 정보
   * @return {@link Page} 형태의 메시지 리스트
   */
  private Page<ChatMessageHistory> paginateMessages(List<ChatMessageHistory> combinedMessages, Pageable pageable) {
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), combinedMessages.size());
    List<ChatMessageHistory> pagedMessages = combinedMessages.subList(start, end);
    return new PageImpl<>(pagedMessages, pageable, combinedMessages.size());
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
    return this.postService.getPostStatusByPostId(postId);
  }
  
}
