package com.gogym.chat.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gogym.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  
  /**
   * 특정 게시글과 요청자의 채팅방 존재 여부 확인.
   *
   * @param postId 게시글 ID
   * @param requestId 요청자 ID
   * @return 채팅방 존재 여부
   */
  boolean existsByPostIdAndRequestId(Long postId, Long requestId);
  
  /**
   * 특정 사용자가 참여한 채팅방 목록 조회.
   *
   * @param postId 게시글 ID
   * @param requestId 요청자 ID
   * @param pageable 페이징 정보
   * @return 참여한 채팅방 목록 (Page)
   */
  Page<ChatRoom> findByPostIdOrRequestId(Long postId, Long requestId, Pageable pageable);
  
  /**
   * 사용자가 참여한 특정 채팅방 조회.
   *
   * @param chatRoomId 채팅방 ID
   * @param postId 게시글 ID
   * @param requestId 요청자 ID
   * @return 특정 채팅방 (Optional)
   */
  Optional<ChatRoom> findByIdAndPostIdOrRequestId(Long chatRoomId, Long postId, Long requestId);
  
}
