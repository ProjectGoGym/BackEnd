package com.gogym.chat.entity;

import com.gogym.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_message_read")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessageRead extends BaseEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "read_id")
  private Long id; // 읽음 상태 ID
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id", nullable = false)
  private ChatRoom chatRoom; // 읽음 상태를 관리하는 채팅방
  
  @Column(name = "member_id", nullable = false)
  private Long memberId; // 메시지를 읽은 사용자 ID
  
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_read_message")
  private ChatMessage lastReadMessage; // 마지막으로 읽은 메시지
  
}
