package com.gogym.chat.service;

import com.gogym.chat.dto.base.MessageRequest;

public interface ChatMessageService {
  
  /**
   * 메시지를 채팅방에 브로드캐스팅.
   * 
   * @param messageRequest 메시지 요청 (다형성 처리)
   * @param memberId 요청 사용자 ID
   */
  void sendMessage(MessageRequest messageRequest, Long memberId);
  
}
