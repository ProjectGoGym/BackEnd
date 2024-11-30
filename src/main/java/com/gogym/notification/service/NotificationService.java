package com.gogym.notification.service;

import static com.gogym.common.response.ErrorCode.ALREADY_READ;
import static com.gogym.common.response.ErrorCode.REQUEST_NOT_FOUND;

import com.gogym.exception.CustomException;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.entity.Notification;
import com.gogym.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;

  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(Long memberId) {

    /*
       TODO : Member 객체 검증 로직
        예시)
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(예외처리));
        검증 메서드를 별도 분리하기
     */
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    emitters.put(memberId, emitter);

    // 클라이언트 연결 종료, 만료, 에러 처리
    emitter.onCompletion(() -> removeEmitter(memberId));
    emitter.onTimeout(() -> removeEmitter(memberId));
    emitter.onError((e) -> removeEmitter(memberId));

    sendDummyData(memberId, emitter);

    return emitter;
  }

  private void removeEmitter(Long memberId) {
    emitters.remove(memberId);
  }

  public void sendDummyData(Long memberId, SseEmitter emitter) {

    // 연결이 되었으면 더미(뻥) 데이터 전송(클라이언트에서 확인용으로 사용하면 될 것 같습니다.)
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event()
            .name("dummy")
            .data("Well Connected! Waiting for notifications."));
      } catch (IOException e) {
        log.error("▶ 더미 데이터 전송 실패 : memberId = {}, 오류 : {}", memberId, e.getMessage());
        removeEmitter(memberId);
      }
    }
  }

  @Scheduled(fixedRate = 30000)
  public void sendHeartbeat() {
    emitters.keySet().forEach(this::sendHeartbeat);
  }

  /*
  클라이언트와 연결이 원활이 되었는지 확인하는 메서드입니다.
  클라이언트 측에서 해당 메세지를 30초마다 한번씩 받지 못하면 재연결 하는 로직을 구현해야 할 것 같습니다.
   */
  public void sendHeartbeat(Long memberId) {

    SseEmitter emitter = emitters.get(memberId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event()
            .name("heartbeat")
            .data("connecting..."));
      } catch (IOException e) {
        log.error("▶ 하트비트 메세지 전송 실패 : memberId = {}, 오류 : {}", memberId, e.getMessage());
        removeEmitter(memberId);
      }
    }
  }

  // 다른 서비스 로직에서 트리거가 되는 메서드에 사용되면 될 것 같습니다.
  @Transactional
  public void createNotification(Long memberId, NotificationDto notificationDto) {

    /*
       TODO : Member 객체 검증 로직
        예시)
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(예외처리));
        검증 메서드를 별도 분리하기
     */

    try {
      // TODO : Member 객체를 저장
      Notification notification = Notification.of(memberId, notificationDto);

      // 알림받을 회원이 구독하지 않은상태(로그인 하지 않은 상태) 이더라도 알림은 저장이 됩니다.
      notificationRepository.save(notification);

      sendNotification(memberId, notification);
    } catch (Exception e) {
      log.error("▶ 알림 생성 중 오류 발생 : memberId = {}, notificationDto = {}, 오류 : {}", memberId, notificationDto, e.getMessage());
    }
  }

  public void sendNotification(Long memberId, Notification notification) {

    // notification 테이블에 저장 후 사용자에게 전송
    SseEmitter emitter = emitters.get(memberId);
    if (emitter != null) {
      try {
        NotificationDto notificationDto = NotificationDto.fromEntity(notification);
        emitter.send(SseEmitter.event()
            .name("notification")
            .data(notificationDto));
      } catch (IOException e) {
        log.error("▶ 알림 전송 중 오류 발생 : memberId = {}, 오류 : {}", memberId, e.getMessage());
        removeEmitter(memberId);
      }
    }
  }

  public Page<NotificationDto> getAllNotifications(Long memberId, Pageable pageable) {

    /*
       TODO : Member 객체 검증 로직
        예시)
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(예외처리));
        검증 메서드를 별도 분리하기
     */

    // 읽지않은 알림목록만 받아옵니다.
    Page<Notification> notificationPage = notificationRepository.findAllByMemberIdAndIsReadFalse(
        memberId, pageable);

    return notificationPage.map(NotificationDto::fromEntity);
  }

  @Transactional
  public void updateNotification(Long notificationId, Long memberId) {

    Notification notification = notificationRepository.findByIdAndMemberId(notificationId, memberId)
        .orElseThrow(() -> new CustomException(REQUEST_NOT_FOUND));

    if (notification.getIsRead()) {
      log.error("▶ 하트비트 메세지 전송 실패 : memberId = {}, notificationId = {}", memberId, notificationId);
      throw new CustomException(ALREADY_READ);
    }
    notification.read();
  }
}