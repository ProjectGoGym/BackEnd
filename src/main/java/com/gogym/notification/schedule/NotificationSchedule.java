package com.gogym.notification.schedule;

import com.gogym.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSchedule {

  private final NotificationService notificationService;

  @Scheduled(fixedRate = 30000)
  public void sendHeartbeatScheduled() {
    notificationService.getEmitters().keySet().forEach(notificationService::sendHeartbeat);
  }
}
