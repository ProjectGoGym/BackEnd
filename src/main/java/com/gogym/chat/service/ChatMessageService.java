package com.gogym.chat.service;

import org.springframework.data.domain.Pageable;
import com.gogym.chat.dto.ChatMessageDto.ChatRoomMessagesResponse;

public interface ChatMessageService {
  
  /**
   * 특정 채팅방의 메시지와 게시물 상태를 조회.
   * 
   * @param memberId 요청 사용자 ID
   * @param chatRoomId 채팅방 ID
   * @param pageable 페이징 정보
   * @return {@link ChatRoomMessagesResponse} 채팅 메시지 목록과 게시물 상태
   */
  ChatRoomMessagesResponse getMessagesWithPostStatus(Long memberId, Long chatRoomId, Pageable pageable);
  
}
