package com.gogym.chat.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatRoomQueryService;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {
  
  private final ChatRoomRepository chatRoomRepository;
  
  @Override
  public ChatRoom getChatRoomById(Long chatRoomId) {
    return this.chatRoomRepository.findById(chatRoomId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
  }
  
  @Override
  public ChatRoom getChatRoomByParticipantsAndId(Long chatRoomId, Long memberId1, Long memberId2) {
    return this.chatRoomRepository.findByChatRoomIdAndParticipants(chatRoomId, memberId1, memberId2)
        .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
  }
  
}
