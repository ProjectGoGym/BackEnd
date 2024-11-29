package com.gogym.notification.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.type.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Notification extends BaseEntity {

  private Long memberId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private Boolean isRead;

  public static Notification of(Long memberId, NotificationDto notificationdto) {

    return new Notification(memberId, notificationdto.getType(), notificationdto.getContent(), false);
  }

  public void read() {
    this.isRead = true;
  }
}