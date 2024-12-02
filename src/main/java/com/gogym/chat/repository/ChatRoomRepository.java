package com.gogym.chat.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.gogym.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  
  // 특정 게시글과 요청자의 채팅방 존재 여부 확인
  boolean existsByPostIdAndRequestId(Long postId, Long requestId);
  
  // 사용자가 참여한 채팅방 목록 조회
  @Query("""
      SELECT c 
      FROM ChatRoom c 
      WHERE c.postId = :memberId OR c.requestId = :memberId
  """)
  Page<ChatRoom> findAllChatroomsByMemberId(@Param("memberId") Long memberId, Pageable pageable);
  
  // 사용자가 참여한 특정 채팅방 조회
  Optional<ChatRoom> findByIdAndPostIdOrRequestId(Long chatRoomId, Long postId, Long requestId);
  
}
