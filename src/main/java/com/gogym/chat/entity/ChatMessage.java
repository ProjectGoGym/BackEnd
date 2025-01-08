package com.gogym.chat.entity;

import com.gogym.chat.type.MessageType;
import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id", nullable = false)
  @Setter
  private ChatRoom chatRoom; // 메시지가 속한 채팅방
  
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content; // 메시지 내용
  
  @Column(name = "sender_id", nullable = false)
  private Long senderId; // 메시지 보낸 사용자 ID
  
  @Column(name = "message_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private MessageType messageType; // 메시지 유형
  
  @Column(name = "safe_payment_id", nullable = true)
  private Long safePaymentId; // 안전거래 ID (nullable)
  
  @Column(name = "safe_payment_status", nullable = true)
  @Enumerated(EnumType.STRING)
  private SafePaymentStatus safePaymentStatus; // 안전거래 상태값 (nullable)
  
  // 안전거래 메시지 여부를 판단하는 메서드
  public boolean isSafePaymentMessage() {
    return this.messageType.toString().startsWith("SYSTEM_SAFE_PAYMENT");
  }
  
}
