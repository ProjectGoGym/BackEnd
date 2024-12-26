package com.gogym.chat.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.gogym.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  
  /**
   * 특정 채팅방에서 사용자가 읽지 않은 메시지 수를 계산.
   * 
   * @param chatRoomId 채팅방 ID
   * @param leaveAt 사용자의 마지막 접속 시간 (leaveAt)
   * @return 읽지 않은 메시지 수
   */
  @Query("""
      SELECT COUNT(m)
      FROM ChatMessage m
      WHERE m.chatRoom.id = :chatRoomId
        AND m.createdAt > :leaveAt
  """)
  int countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("leaveAt") LocalDateTime leaveAt);
  
  /**
   * 특정 채팅방에서 가장 최근에 생성된 메시지 조회.
   *
   * @param chatRoomId 채팅방 ID
   * @return 가장 최근의 메시지 (Optional)
   */
  Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
  
  /**
   * 특정 채팅방의 메시지를 페이징하여 최근 메시지부터 조회.
   * 
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return Page 형태로 반환된 {@link ChatMessage}
   */
  Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
  
}
