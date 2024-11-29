package com.gogym.notification.controller;

import static com.gogym.common.response.SuccessCode.SUCCESS;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.gogym.common.response.ApplicationResponse;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @GetMapping(value = "/subscribe/{memberId}", produces = TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@PathVariable Long memberId) {

    return notificationService.subscribe(memberId);
  }

  @GetMapping
  public ApplicationResponse<Page<NotificationDto>> getAllNotifications(Pageable pageable) {

    Long memberId = 1L;

    Page<NotificationDto> notifications = notificationService.getAllNotifications(memberId,
        pageable);

    return ApplicationResponse.ok(notifications, SUCCESS);
  }

  @PutMapping("/{id}/read")
  public ApplicationResponse<Void> updateNotification(@PathVariable Long id) {

    Long memberId = 1L;

    notificationService.updateNotification(id, memberId);

    return ApplicationResponse.noData(SUCCESS);
  }
}