package com.gogym.notification.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.member.entity.Member;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.type.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "notifications")
public class Notification extends BaseEntity {

  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String content;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead;

  public static Notification of(Member member, NotificationDto notificationdto) {

    return new Notification(member, notificationdto.type(), notificationdto.content(), false);
  }

  public void read() {
    this.isRead = true;
  }
}