package com.gogym.chat.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.gogym.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  
  @Query("""
      SELECT m
      FROM ChatMessage m
      WHERE m.chatRoom.id = :chatRoomId
      ORDER BY m.createdAt DESC
  """)
  Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(@Param("chatRoomId") Long chatRoomId);
  
}
