package com.gogym.chat.service;

import com.gogym.chat.entity.ChatRoom;

public interface ChatRoomQueryService {
  
  /**
   * 특정 채팅방 ID로 채팅방 정보 조회.
   * 
   * @param chatRoomId 채팅방 ID
   * @return 채팅방 정보
   */
  ChatRoom getChatRoomById(Long chatRoomId);
  
  /**
   * 채팅방 ID와 사용자 2명을 기준으로 채팅방 조회.
   * 
   * @param chatRoomId 채팅방 ID
   * @param memberId1 사용자 1 ID
   * @param memberId2 사용자 2 ID
   * @return 채팅방 정보
   */
  ChatRoom getChatRoomByParticipantsAndId(Long chatRoomId, Long memberId1, Long memberId2);
  
}
