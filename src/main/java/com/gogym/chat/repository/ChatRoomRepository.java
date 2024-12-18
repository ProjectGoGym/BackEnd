package com.gogym.chat.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.gogym.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  
  /**
   * 특정 게시글과 요청자의 채팅방 존재 여부 확인.
   *
   * @param postId 게시글 ID
   * @param requestorId 요청자 ID
   * @return 채팅방 존재 여부
   */
  @Query("""
      SELECT COUNT(cr) > 0
      FROM ChatRoom cr
      WHERE cr.post.id = :postId
        AND cr.requestor.id = :requestorId
  """)
  boolean existsByPostIdAndRequestorId(
      @Param("postId") Long postId,
      @Param("requestorId") Long requestorId
  );
  
  /**
   * 사용자가 참여한 채팅방 목록을 마지막 메시지가 전송된 시간을 기준으로 정렬하여 조회.
   * 
   * @param postAuthorId 게시글 작성자 ID
   * @param requestorId 요청자 ID
   * @param pageable 페이징 및 정렬 정보
   * @return 참여한 채팅방 목록 (Page)
   */
  @Query("""
      SELECT cr
      FROM ChatRoom cr
      LEFT JOIN cr.post p
      LEFT JOIN p.author a
      LEFT JOIN ChatMessage cm ON cm.chatRoom.id = cr.id
      WHERE (a.id = :postAuthorId OR cr.requestor.id = :requestorId)
        AND cr.isDeleted = false
      GROUP BY cr.id, p.id, a.id, cr.requestor.id, cr.isDeleted
      ORDER BY MAX(cm.createdAt) DESC
  """)
  Page<ChatRoom> findChatRoomsSortedByLastMessage(
      @Param("postAuthorId") Long postAuthorId,
      @Param("requestorId") Long requestorId,
      Pageable pageable
  );
  
  /**
   * 사용자가 참여한 특정 채팅방 조회.
   *
   * @param chatRoomId 채팅방 ID
   * @param postAuthorId 게시글 작성자 ID
   * @param requestorId 요청자 ID
   * @return 특정 채팅방 (Optional)
   */
  @Query("""
      SELECT cr
      FROM ChatRoom cr
      WHERE cr.id = :chatRoomId
        AND (cr.post.author.id = :postAuthorId OR cr.requestor.id = :requestorId)
  """)
  Optional<ChatRoom> findByIdAndPostMemberIdOrRequestorId(
      @Param("chatRoomId") Long chatRoomId,
      @Param("postAuthorId") Long postAuthorId,
      @Param("requestorId") Long requestorId
  );
  
  /**
   * 특정 사용자가 특정 채팅방에 참여 중인지 확인.
   *
   * @param chatRoomId 확인할 채팅방 ID
   * @param memberId 확인할 사용자 ID
   * @return true: 사용자가 해당 채팅방에 참여 중인 경우, false: 참여하지 않은 경우
   */
  @Query("""
      SELECT EXISTS (
          SELECT 1
          FROM ChatRoom cr
          WHERE cr.id = :chatRoomId
            AND (cr.post.author.id = :memberId OR cr.requestor.id = :memberId)
            AND cr.isDeleted = false
      )
  """)
  boolean existsByChatRoomIdAndMemberId(
      @Param("chatRoomId") Long chatRoomId,
      @Param("memberId") Long memberId
  );
  
}
