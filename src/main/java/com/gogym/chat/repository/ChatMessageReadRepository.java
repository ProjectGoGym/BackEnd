package com.gogym.chat.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.gogym.chat.entity.ChatMessageRead;
import com.gogym.chat.entity.ChatRoom;

public interface ChatMessageReadRepository extends JpaRepository<ChatMessageRead, Long> {
  
  // 특정 채팅방에서 특정 사용자가 읽은 마지막 메시지 조회
  Optional<ChatMessageRead> findByChatRoomAndMemberId(ChatRoom chatRoom, Long memberId);
  
  @Query("""
      SELECT COUNT(m)
      FROM ChatMessage m
      WHERE m.chatRoom.id = :chatRoomId
        AND m.id > COALESCE(( 
            SELECT r.lastReadMessage.id 
            FROM ChatMessageRead r 
            WHERE r.chatRoom.id = :chatRoomId 
              AND r.memberId = :memberId 
        ), 0)
  """)
  int countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);
  
}
