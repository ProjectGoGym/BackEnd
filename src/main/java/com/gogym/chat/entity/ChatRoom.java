package com.gogym.chat.entity;

import com.gogym.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatrooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseEntity {
  
  @Column(name = "post_id", nullable = false)
  private Long postId; // 게시글 작성자 ID
  
  @Column(name = "request_id", nullable = false)
  private Long requestId; // 채팅 요청자 ID
  
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted; // 삭제 여부
  
}
