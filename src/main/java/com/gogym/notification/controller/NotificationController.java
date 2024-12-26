package com.gogym.notification.controller;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping(value = "/subscribe", produces = TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(HttpServletResponse response,
      @RequestParam("id") Long memberId) {

    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Connection", "keep-alive");
    response.setHeader("Content-Type", TEXT_EVENT_STREAM_VALUE);

    return notificationService.subscribe(memberId);
  }

  @GetMapping
  public ResponseEntity<Page<NotificationDto>> getAllNotifications(@LoginMemberId Long memberId,
      Pageable pageable) {

    Page<NotificationDto> notifications = notificationService.getAllNotifications(memberId,
        pageable);

    return ResponseEntity.ok(notifications);
  }

  @PutMapping("/{notification-id}/read")
  public ResponseEntity<Void> updateNotification(@LoginMemberId Long memberId,
      @PathVariable("notification-id") Long notificationId) {

    notificationService.updateNotification(notificationId, memberId);

    return ResponseEntity.ok().build();
  }
}