package com.gogym.chat.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gogym.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  
  /**
   * 특정 채팅방에서 가장 최근에 생성된 메시지를 조회합니다.
   *
   * @param chatRoomId 채팅방 ID
   * @return 가장 최근의 메시지 (Optional)
   */
  Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
  
}
