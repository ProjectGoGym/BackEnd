package com.gogym.notification.controller;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  // TODO : 파라미터로 memberId 를 받는것이 아닌 token 으로 받는것으로 수정 {member-id} 제거
  @GetMapping(value = "/subscribe/{member-id}", produces = TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@PathVariable("member-id") Long memberId) {

    return notificationService.subscribe(memberId);
  }

  @GetMapping
  public ResponseEntity<Page<NotificationDto>> getAllNotifications(Pageable pageable) {

    // TODO : 하드코딩 추후 변경
    Long memberId = 1L;

    Page<NotificationDto> notifications = notificationService.getAllNotifications(memberId,
        pageable);

    return ResponseEntity.ok(notifications);
  }

  @PutMapping("/{notification-id}/read")
  public ResponseEntity<Void> updateNotification(@PathVariable("notification-id") Long notificationId) {

    // TODO : 하드코딩 추후 변경
    Long memberId = 1L;

    notificationService.updateNotification(notificationId, memberId);

    return ResponseEntity.noContent().build();
  }
}