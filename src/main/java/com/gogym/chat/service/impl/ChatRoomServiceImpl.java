package com.gogym.chat.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageHistory;
import com.gogym.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.gogym.chat.dto.ChatRoomDto.LeaveRequest;
import com.gogym.chat.entity.ChatMessage;
import com.gogym.chat.entity.ChatMessageRead;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatMessageReadRepository;
import com.gogym.chat.repository.ChatMessageRepository;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatRedisService;
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
public class ChatRoomServiceImpl implements ChatRoomService {
  
  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ChatMessageReadRepository chatMessageReadRepository;
  
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
        null); // lastMessageAt
  }
  
  @Override
  public List<ChatRoomResponse> getChatRooms(Long memberId, int page, int size) {
    // 페이징 조건으로 사용자가 참여한 채팅방 목록 조회
    Page<ChatRoom> chatRooms = this.chatRoomRepository.findByPostMemberIdOrRequestorIdAndIsDeletedFalse(
        memberId,
        memberId,
        PageRequest.of(page, size));
    
    // 채팅방 목록 데이터 반환
    return chatRooms.stream().map(chatRoom -> {
      // 상대방 정보 조회
      Member counterparty = chatRoom.getRequestor().getId().equals(memberId)
          ? chatRoom.getPost().getMember() // 게시글 작성자가 상대방인 경우
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
        ChatMessageHistory lastMessageHistory = JsonUtil.deserialize(lastMessageJson, ChatMessageHistory.class);
        
        if (lastMessageHistory != null) {
          lastMessage = ChatMessage.builder()
              .content(lastMessageHistory.content())
              .senderId(lastMessageHistory.senderId())
              .chatRoom(chatRoom)
              .build();

          // 메시지 생성 시간 설정
          lastMessageAt = LocalDateTime.parse(
              lastMessageHistory.createdAt(),
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
      
      // 읽지 않은 메시지 개수
      int unreadMessageCount = this.chatMessageReadRepository.countUnreadMessages(
          chatRoom.getId(),
          memberId);
      
      // ChatRoomResponse 생성
      return new ChatRoomResponse(
          chatRoom.getId(), // chatRoomId
          chatRoom.getCreatedAt(), // createdAt
          chatRoom.getPost().getId(), // postId
          counterparty.getId(), // counterpartyId
          counterparty.getNickname(), // counterpartyNickname
          unreadMessageCount, // unreadMessageCount
          lastMessage != null ? lastMessage.getContent() : null, // lastMessage
          lastMessageAt); // lastMessageAt
    }).collect(Collectors.toList());
  }
  
  @Override
  public void leaveChatRoom(Long memberId, Long chatRoomId, LeaveRequest request) {
    // 나가려는 채팅방 존재 여부 확인
    ChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
    
    // 요청자가 채팅방에 속해 있는지 확인
    if (!chatRoom.getPost().getMember().getId().equals(memberId)
        && !chatRoom.getRequestor().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // Redis에서 메시지 목록 조회
    List<String> redisMessages = this.chatRedisService.getMessages(chatRoomId);
    
    // Redis 메시지들을 DB에 저장
    if (redisMessages != null && !redisMessages.isEmpty()) {
      this.forceSaveMessages(chatRoomId, redisMessages);
    }
    
    // DB에서 가장 최근 메시지 가져오기
    ChatMessage lastMessage = this.chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
    
    // 마지막으로 읽은 메시지 업데이트
    ChatMessageRead messageRead = this.chatMessageReadRepository.findByChatRoomAndMemberId(chatRoom, memberId)
        .orElseGet(() -> ChatMessageRead.builder()
            .chatRoom(chatRoom)
            .memberId(memberId)
            .build());
    messageRead.setLastReadMessage(lastMessage);

    // 변경된 엔티티 저장
    this.chatMessageReadRepository.save(messageRead);
  }
  
  @Override
  public void deleteChatRoom(Long memberId, Long chatRoomId) {
    // 채팅방 존재 여부 확인
    ChatRoom chatRoom = this.chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
    
    // 회원이 해당 채팅방에 속해 있는지 확인
    if (!chatRoom.getPost().getMember().getId().equals(memberId)
        && !chatRoom.getRequestor().getId().equals(memberId)) {
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
    if (chatRoom.getPost().getMember().getId().equals(memberId)) {
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
          ChatMessageHistory messageHistory = JsonUtil.deserialize(messageJson, ChatMessageHistory.class);
          
          if (messageHistory != null) {
            // ChatMessageHistory -> ChatMessage 변환
            return ChatMessage.builder()
                .content(messageHistory.content())
                .senderId(messageHistory.senderId())
                .chatRoom(this.chatRoomRepository.getReferenceById(chatRoomId))
                .build();
            }
          return null;
        })
        .filter(chatMessage -> chatMessage != null)
        .collect(Collectors.toList());

    // DB에 저장
    this.chatMessageRepository.saveAll(chatMessages);
  }

}
