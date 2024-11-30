package com.gogym.notification.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.type.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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


  // TODO : 추후 join column 으로 Member 객체 연결
  private Long memberId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String content;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead;

  public static Notification of(Long memberId, NotificationDto notificationdto) {

    return new Notification(memberId, notificationdto.type(), notificationdto.content(), false);
  }

  public void read() {
    this.isRead = true;
  }
}