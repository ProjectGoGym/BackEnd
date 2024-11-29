package com.gogym.notification.dto;

import com.gogym.notification.entity.Notification;
import com.gogym.notification.type.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

  private NotificationType type;

  private String content;

  private LocalDateTime createdAt;

  public static NotificationDto fromEntity(Notification notification) {
    return new NotificationDto(notification.getType(), notification.getContent(),
        notification.getCreatedAt());
  }
}
