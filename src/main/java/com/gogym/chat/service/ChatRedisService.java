package com.gogym.chat.service;

import java.util.List;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;

public interface ChatRedisService {
  
  /**
   * 메시지를 Redis에 저장 후 응답
   * 
   * @param messageRequest 요청 메시지
   * @param memberId 메시지를 보낸 사용자 ID
   * @return 저장된 메시지 응답
   */
  ChatMessageResponse saveMessageToRedis(ChatMessageRequest messageRequest, Long memberId);
  
  /**
   * Redis에서 특정 채팅방의 메시지 목록을 조회
   * 
   * @param chatRoomId 메시지를 조회할 채팅방의 ID
   * @return Redis에 저장된 메시지 목록
   */
  List<String> getMessages(Long chatRoomId);
  
  /**
   * Redis에 저장된 특정 채팅방의 메시지를 삭제
   * 
   * @param chatRoomId 저장 및 삭제 대상이 되는 채팅방의 ID
   */
  void deleteMessages(Long chatRoomId);
  
  /**
   * Redis에서 채팅방 메시지를 저장할 때 사용하는 키의 접두사를 반환
   * 
   * @return Redis에서 채팅방 메시지 키의 접두사를 나타내는 문자열
   */
  String getRedisChatroomMessageKeyPrefix();

}
