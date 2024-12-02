package com.gogym.notification.dto;

import com.gogym.notification.entity.Notification;
import com.gogym.notification.type.NotificationType;
import java.time.LocalDateTime;

public record NotificationDto(

    NotificationType type,
    String content,
    LocalDateTime createdAt

) {

  public static NotificationDto fromEntity(Notification notification) {
    return new NotificationDto(notification.getType(), notification.getContent(),
        notification.getCreatedAt());
  }
}
