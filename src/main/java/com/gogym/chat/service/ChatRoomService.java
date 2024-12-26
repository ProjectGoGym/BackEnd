package com.gogym.chat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gogym.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.gogym.chat.dto.ChatRoomDto.LeaveRequest;

public interface ChatRoomService {
  
  /**
   * 채팅방 생성
   * 
   * @param memberId 요청자 ID
   * @param postId 게시글 ID
   * @return 생성된 채팅방 정보
   */
  ChatRoomResponse createChatRoom(Long memberId, Long postId);
  
  /**
   * 사용자가 참여한 채팅방 목록 조회
   * 
   * @param memberId 요청자 ID
   * @param pageable 페이징 정보
   * @return 사용자가 참여한 채팅 목록
   */
  Page<ChatRoomResponse> getChatRooms(Long memberId, Pageable pageable);
  
  /**
   * 채팅방 나가기
   * 
   * @param memberId 요청자 ID
   * @param ChatroomId 채팅방 ID
   * @param request 나가기 요청 정보
   */
  void leaveChatRoom(Long memberId, Long chatRoomId, LeaveRequest request);
  
  /**
   * 채팅방 삭제
   * 
   * @param memberId 요청자 ID
   * @param chatRoomId 채팅방 ID
   */
  void deleteChatRoom(Long memberId, Long chatRoomId);
}
