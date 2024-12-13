package com.gogym.chat.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.member.entity.Member;
import com.gogym.post.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "chatrooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseEntity {
  
  @JoinColumn(name = "post_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Post post; // 게시글 작성자
  
  @JoinColumn(name = "requestor_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member requestor; // 채팅 요청자
  
  @Column(name = "is_deleted", nullable = false)
  @Setter
  private Boolean isDeleted; // 삭제 여부
  
  @Column(name = "post_author_active", nullable = false)
  @Builder.Default
  @Setter
  private Boolean postAuthorActive = true; // 게시글 작성자의 채팅방 활성화 여부
  
  @Column(name = "requestor_active", nullable = true)
  @Builder.Default
  @Setter
  private Boolean requestorActive = true; // 채팅 요청자의 채팅방 활성화 여부
  
  // 게시글 작성자 반환 메서드
  public Member getPostAuthor() {
    return this.post.getMember();
  }
  
}
