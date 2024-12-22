package com.gogym.notification.service;

import static com.gogym.exception.ErrorCode.ALREADY_READ;
import static com.gogym.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.entity.Notification;
import com.gogym.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;

  private final MemberService memberService;

  private static final Long SSE_TIME_OUT = 60000L;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Getter
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(Long memberId) {

    memberService.findById(memberId);

    SseEmitter emitter = new SseEmitter(SSE_TIME_OUT);
    emitters.put(memberId, emitter);

    // 클라이언트 연결 종료, 만료, 에러 처리
    emitter.onCompletion(() -> {
      log.info("👍SSE 구독 정상 해제 (memberId : {}),", memberId);
      removeEmitter(memberId);
    });
    emitter.onTimeout(() -> {
      log.warn("🕰️SSE 구독 타임 아웃 (memberId : {}),", memberId);
      removeEmitter(memberId);
    });
    emitter.onError((e) -> {
      log.error("🚨SSE 구독 알수없는 에러 발생 \n📍memberId: {}, \n📍에러: {}", memberId, e.getMessage());
      removeEmitter(memberId);
    });

    sendDummyData(memberId, emitter);

    return emitter;
  }

  public void removeEmitter(Long memberId) {
    emitters.remove(memberId);
  }

  public void sendDummyData(Long memberId, SseEmitter emitter) {

    // 연결이 되었으면 더미(뻥) 데이터 전송(클라이언트에서 확인용으로 사용하면 될 것 같습니다.)
    if (emitter != null) {

      scheduler.schedule(() -> {
        try {
          emitter.send(SseEmitter.event()
              .name("dummy")
              .data("connecting...")
              .reconnectTime(3000L));
          log.info("✅ 더미 이벤트 발송 완료!: {}", memberId);
        } catch (IOException e) {
          log.error("🚨 더미 이벤트 발송 중 예외 발생!: {}", e.getMessage());
          removeEmitter(memberId);
        }
      }, 2, TimeUnit.SECONDS);
    }
  }

  @Transactional
  // 다른 서비스 로직에서 트리거가 되는 메서드에 사용되면 될 것 같습니다.
  public void createNotification(Long memberId, NotificationDto notificationDto) {

    Member member = memberService.findById(memberId);

    Notification notification = Notification.of(member, notificationDto);

    // 알림받을 회원이 구독하지 않은상태(로그인 하지 않은 상태) 이더라도 알림은 저장이 됩니다.
    notificationRepository.save(notification);

    sendNotification(memberId, notification);
  }

  public void sendNotification(Long memberId, Notification notification) {

    // notification 테이블에 저장 후 사용자에게 전송
    SseEmitter emitter = emitters.get(memberId);
    if (emitter != null) {
      try {
        NotificationDto notificationDto = NotificationDto.fromEntity(notification);
        emitter.send(SseEmitter.event()
            .name("notification")
            .data(notificationDto)
            .reconnectTime(3000L));
        log.info("✅ 알림 이벤트 발송 완료!: {}", memberId);
      } catch (IOException e) {
        log.error("🚨 알림 이벤트 발송 중 예외 발생!: {}", e.getMessage());
        removeEmitter(memberId);
      }
    }
  }

  public Page<NotificationDto> getAllNotifications(Long memberId, Pageable pageable) {

    memberService.findById(memberId);

    // 읽지않은 알림목록만 받아옵니다.
    Page<Notification> notificationPage = notificationRepository.findAllByMemberIdAndIsReadFalse(
        memberId, pageable);

    return notificationPage.map(NotificationDto::fromEntity);
  }

  @Transactional
  public void updateNotification(Long notificationId, Long memberId) {

    Notification notification = notificationRepository.findByIdAndMemberId(notificationId, memberId)
        .orElseThrow(() -> new CustomException(NOTIFICATION_NOT_FOUND));

    if (notification.getIsRead()) {
      throw new CustomException(ALREADY_READ);
    }
    notification.read();
  }
}