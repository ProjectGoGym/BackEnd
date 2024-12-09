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
   * @param requestorId 요청자 ID
   * @return 채팅방 존재 여부
   */
  boolean existsByPostIdAndRequestorId(Long postId, Long requestorId);
  
  /**
   * 특정 사용자가 참여한 채팅방 목록 조회.
   *
   * @param postAuthorId 게시글 작성자 ID
   * @param requestorId 요청자 ID
   * @param pageable 페이징 정보
   * @return 참여한 채팅방 목록 (Page)
   */
  Page<ChatRoom> findByPostMemberIdOrRequestorIdAndIsDeletedFalse(Long postAuthorId, Long requestorId, Pageable pageable);
  
  /**
   * 사용자가 참여한 특정 채팅방 조회.
   *
   * @param chatRoomId 채팅방 ID
   * @param postAuthorId 게시글 작성자 ID
   * @param requestorId 요청자 ID
   * @return 특정 채팅방 (Optional)
   */
  Optional<ChatRoom> findByIdAndPostMemberIdOrRequestorId(Long chatRoomId, Long postAuthorId, Long requestorId);
  
}
