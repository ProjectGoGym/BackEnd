package com.gogym.chat.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gogym.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.gogym.chat.dto.ChatRoomDto.LeaveRequest;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.common.annotation.LoginMemberId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatRoomController {
  
  private final ChatRoomService chatRoomService;
  
  /**
   * 특정 게시글에 대한 채팅방을 생성합니다.
   * 
   * POST /api/chatroom/{post-id}
   * 
   * @param memberId 요청자 ID
   * @param postId 채팅방을 생성할 게시글 ID
   * @return 생성된 {@link ChatRoomResponse} 객체를 포함한 {@link ResponseEntity}.
   */
  @PostMapping("/{post-id}")
  public ResponseEntity<ChatRoomResponse> createChatRoom(
      @LoginMemberId Long memberId,
      @PathVariable("post-id") Long postId) {
    return ResponseEntity.ok(this.chatRoomService.createChatRoom(memberId, postId));
  }
  
  /**
   * 사용자가 참여한 채팅방 목록을 페이지네이션하여 조회합니다.
   * 
   * GET /api/chatroom?page={page}&size={size}
   * 
   * @param memberId 요청자 ID
   * @param page 조회할 페이지 번호
   * @param size 페이지당 항목 수
   * @return {@link ChatroomResponse} 객체 리스트를 포함한 {@link ResponseEntity}.
   */
  @GetMapping
  public ResponseEntity<List<ChatRoomResponse>> getChatRooms(
      @LoginMemberId Long memberId,
      @RequestParam("page") int page,
      @RequestParam("size") int size) {
    return ResponseEntity.ok(this.chatRoomService.getChatRooms(memberId, page, size));
  }
  
  /**
   * 사용자가 채팅방에서 나갈 때 마지막 읽은 메시지를 업데이트합니다.
   * 
   * POST /api/chatroom/{chatroom-id}/leave
   * 
   * @param memberId 요청자 ID
   * @param chatroomId 나갈 채팅방 ID.
   * @param request 탈퇴 요청 세부 정보를 포함한 {@link LeaveRequest}.
   * @return 성공 상태를 나타내는 {@link ResponseEntity}.
   */
  @PostMapping("/{chatroom-id}/leave")
  public ResponseEntity<Void> leaveChatRoom(
      @LoginMemberId Long memberId,
      @PathVariable("chatroom-id") Long chatRoomId,
      @Valid @RequestBody LeaveRequest request) {
    this.chatRoomService.leaveChatRoom(memberId, chatRoomId, request);
    return ResponseEntity.ok().build();
  }
  
  /**
   * 특정 채팅방을 삭제합니다.
   * 
   * DELETE /api/chatroom/{chatroom-id}
   * 
   * @param memberId 요청자 ID
   * @param chatroomId 삭제할 채팅방 ID
   * @return 성공 상태를 나타내는 {@link ResponseEntity}.
   */
  @DeleteMapping("/{chatroom-id}")
  public ResponseEntity<Void> deleteChatRoom(
      @LoginMemberId Long memberId,
      @PathVariable("chatroom-id") Long chatRoomId) {
    this.chatRoomService.deleteChatRoom(memberId, chatRoomId);
    return ResponseEntity.ok().build();
  }
  
}
