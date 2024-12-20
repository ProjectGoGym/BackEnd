package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.gogym.chat.dto.ChatMessageDto.RedisChatMessage;
import com.gogym.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.gogym.chat.dto.ChatRoomDto.LeaveRequest;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatRedisService;
import com.gogym.chat.service.ChatRoomQueryService;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.post.service.PostService;
import com.gogym.util.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomQueryService, ChatRoomService {
  
  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  
  private final ChatRedisService chatRedisService;
  private final MemberService memberService;
  private final PostService postService;
  
  @Override
  public ChatRoomResponse createChatRoom(Long memberId, Long postId) {
    // 게시글 작성자 존재 여부 확인
    Member postAuthor = this.postService.getPostAuthor(postId);
    
    // 이미 존재하는 채팅방 여부 확인
    if (this.chatRoomRepository.existsByPostIdAndRequestorId(postId, memberId)) {
      throw new CustomException(ErrorCode.CHATROOM_ALREADY_EXISTS);
    }
    
    // 채팅방 생성 및 저장
    ChatRoom newChatRoom = ChatRoom.builder()
        .post(this.postService.findById(postId))
        .requestor(this.memberService.findById(memberId))
        .isDeleted(false)
        .build();
    this.chatRoomRepository.save(newChatRoom);
    
    return new ChatRoomResponse(
        newChatRoom.getId(), // chatRoomId
        newChatRoom.getCreatedAt(), // createdAt
        postId, // postId
        postAuthor.getId(), // counterpartyId
        postAuthor.getNickname(), // counterpartyNickname
        0, // unreadMessageCount
        null, // lastMessage
        null,// lastMessageAt
        newChatRoom.getPostAuthorActive(), // postAuthorActive
        newChatRoom.getRequestorActive() // requestorActive
    );
  }
  
  @Override
  public Page<ChatRoomResponse> getChatRooms(Long memberId, Pageable pageable) {
    // 페이징 조건으로 사용자가 참여한 채팅방 목록 조회
    Page<ChatRoom> chatRooms = this.chatRoomRepository.findChatRoomsSortedByLastMessage(
        memberId,
        memberId,
        pageable);
    
    // 채팅방 목록 데이터 반환
    return chatRooms.map(chatRoom -> {
      // 상대방 정보 조회
      Member counterparty = chatRoom.getRequestor().getId().equals(memberId)
          ? chatRoom.getPost().getAuthor() // 게시글 작성자가 상대방인 경우
          : chatRoom.getRequestor(); // 요청자가 상대방인 경우
      
      // 상대방 정보가 없을 경우 추가 방어 로직
      if (counterparty == null) {
        throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
      }
      
      // Redis에서 채팅방에 해당하는 메시지들 조회
      List<String> redisMessages = this.chatRedisService.getMessages(chatRoom.getId());
      
      // 마지막 메시지 처리
      ChatMessage lastMessage = null;
      LocalDateTime lastMessageAt = null;
      
      if (redisMessages != null && !redisMessages.isEmpty()) {
        // Redis에서 가장 마지막 메시지 가져오기
        String lastMessageJson = redisMessages.get(redisMessages.size() - 1);
        RedisChatMessage lastMessageHistory = JsonUtil.deserialize(lastMessageJson, RedisChatMessage.class);
        
        if (lastMessageHistory != null) {
          lastMessage = ChatMessage.builder()
              .content(lastMessageHistory.content())
              .senderId(lastMessageHistory.senderId())
              .chatRoom(chatRoom)
              .build();
          
          // 메시지 생성 시간 설정
          lastMessageAt = lastMessageHistory.createdAt();
        }
      }

      // Redis에 메시지가 없으면 DB에서 메시지 조회
      if (lastMessage == null) {
        ChatMessage dbLastMessage = this.chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId())
            .orElse(null);

        if (dbLastMessage != null) {
          lastMessage = dbLastMessage;
          lastMessageAt = dbLastMessage.getCreatedAt();
        }
      }
      
      // 읽지 않은 메시지 수 계산
      LocalDateTime leaveAt = chatRoom.getLeaveAt(memberId);
      int unreadMessageCount = (int) this.chatMessageRepository.countUnreadMessages(
          chatRoom.getId(),
          leaveAt != null ? leaveAt : null);
      
      return new ChatRoomResponse(
          chatRoom.getId(), // chatRoomId
          chatRoom.getCreatedAt(), // createdAt
          chatRoom.getPost().getId(), // postId
          counterparty.getId(), // counterpartyId
          counterparty.getNickname(), // counterpartyNickname
          unreadMessageCount, // unreadMessageCount
          lastMessage != null ? lastMessage.getContent() : null, // lastMessage
          lastMessageAt, // lastMessageAt
          chatRoom.getPostAuthorActive(), // postAuthorActive
          chatRoom.getRequestorActive() // requestorActive
      );
    });
  }
  
  @Override
  public void leaveChatRoom(Long memberId, Long chatRoomId, LeaveRequest request) {
    // 채팅방 존재 여부 확인
    ChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
    
    // 회원이 해당 채팅방에 속해 있는지 확인
    if (!this.isMemberInChatRoom(chatRoomId, memberId)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // Redis에서 메시지 목록 조회
    List<String> redisMessages = this.chatRedisService.getMessages(chatRoomId);
    
    // Redis 메시지들을 DB에 저장
    if (redisMessages != null && !redisMessages.isEmpty()) {
      this.forceSaveMessages(chatRoomId, redisMessages);
    }
    
    // 채팅방을 나간 시점 저장
    chatRoom.setLeaveAt(memberId, request.leaveAt());
  }
  
  @Override
  public void deleteChatRoom(Long memberId, Long chatRoomId) {
    // 채팅방 존재 여부 확인
    ChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
    
    // 회원이 해당 채팅방에 속해 있는지 확인
    if (!this.isMemberInChatRoom(chatRoomId, memberId)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // Redis에서 채팅 메시지 조회
    List<String> redisMessages = this.chatRedisService.getMessages(chatRoomId);
    
    // Redis에 메시지가 존재할 경우 DB에 메시지 저장 및 Redis에서 삭제
    if (redisMessages != null && !redisMessages.isEmpty()) {
      this.forceSaveMessages(chatRoomId, redisMessages);
      this.chatRedisService.deleteMessages(chatRoomId);
    }
    
    // 회원별 채팅방 활성화 상태 업데이트
    if (chatRoom.getPost().getAuthor().getId().equals(memberId)) {
      chatRoom.setPostAuthorActive(false);
    } else if (chatRoom.getRequestor().getId().equals(memberId)) {
      chatRoom.setRequestorActive(false);
    }
    
    // 양쪽 모두 나갔다면 채팅방 삭제 상태로 변경
    if (!chatRoom.getPostAuthorActive() && !chatRoom.getRequestorActive()) {
      chatRoom.setIsDeleted(true);
    }
  }
  
  /**
   * Redis에 저장된 메시지들을 강제로 DB에 저장.
   * 
   * @param chatRoomId 채팅방 ID
   * @param redisMessages Redis 메시지 리스트
   */
  private void forceSaveMessages(Long chatRoomId, List<String> redisMessages) {
    List<ChatMessage> chatMessages = redisMessages.stream()
        .map(messageJson -> {
          RedisChatMessage messageHistory = JsonUtil.deserialize(messageJson, RedisChatMessage.class);
          
          if (messageHistory != null) {
            // RedisChatMessage -> ChatMessage 변환
            return ChatMessage.builder()
                .chatRoom(this.chatRoomRepository.getReferenceById(chatRoomId))
                .content(messageHistory.content())
                .senderId(messageHistory.senderId())
                .build();
            }
          return null;
        })
        .filter(chatMessage -> chatMessage != null)
        .collect(Collectors.toList());

    // DB에 저장
    this.chatMessageRepository.saveAll(chatMessages);
  }
  
  @Override
  public ChatRoom getChatRoomById(Long chatRoomId) {
    return this.chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
  }
  
  @Override
  public ChatRoom getChatRoomByParticipantsAndId(Long chatRoomId, Long memberId1, Long memberId2) {
    return this.chatRoomRepository.findByChatRoomIdAndParticipants(chatRoomId, memberId1, memberId2)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
  }
  
  @Override
  public boolean isMemberInChatRoom(Long chatRoomId, Long memberId) {
    return this.chatRoomRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId);
  }

}
