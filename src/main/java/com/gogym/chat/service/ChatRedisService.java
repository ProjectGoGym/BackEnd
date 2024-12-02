package com.gogym.chat.service;

import com.gogym.chat.dto.ChatMessageDto.ChatMessageRequest;
import com.gogym.chat.dto.ChatMessageDto.ChatMessageResponse;

public interface ChatRedisService {
  
  /**
   * 메시지를 Redis에 저장 후 응답
   * 
   * @param messageRequest 요청 메시지
   * @return 저장된 메시지 응답
   */
  ChatMessageResponse saveMessageToRedis(ChatMessageRequest messageRequest);

}
