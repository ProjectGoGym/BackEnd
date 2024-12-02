package com.gogym.notification.service;

import static com.gogym.exception.ErrorCode.ALREADY_READ;
import static com.gogym.exception.ErrorCode.NOTIFICATION_NOT_FOUND;
import static com.gogym.notification.type.NotificationType.ADD_WISHLIST_MY_POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.entity.Notification;
import com.gogym.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private SseEmitter sseEmitter;

  @InjectMocks
  private NotificationService notificationService;

  private Notification notification;
  private Notification readNotification;
  private NotificationDto notificationDto;
  private Member member;
  private final Pageable pageable = Pageable.ofSize(10);

  @BeforeEach
  void setUp() {

    member = Member.builder().id(1L).build();
    notificationDto = new NotificationDto(ADD_WISHLIST_MY_POST, "test message", null);
    notification = Notification.of(member, notificationDto);
    readNotification = new Notification(member, ADD_WISHLIST_MY_POST, "test message", true);
  }

  private Map<Long, SseEmitter> emitters() {
    return (Map<Long, SseEmitter>) ReflectionTestUtils.getField(notificationService, "emitters");
  }

  @Test
  @DisplayName("구독을 신청한 회원은 Map 에 등록되어 관리된다.")
  void subscribe_shouldAddEmitterToMap() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    // when
    sseEmitter = notificationService.subscribe(member.getId());
    // then
    assertNotNull(sseEmitter);
    Map<Long, SseEmitter> emitters = emitters();
    assertTrue(emitters.containsKey(member.getId()));
  }

  @Test
  @DisplayName("알림 생성 시 DB 에 저장된다.")
  void createNotification_shouldSaveNotification() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    // when
    notificationService.createNotification(member.getId(), notificationDto);
    // then
    verify(notificationRepository).save(any());
  }

  @Test
  @DisplayName("저장된 알림이 있고 읽지 않은 경우 알림이 조회된다.")
  void getAllNotifications_shouldReturnUnReadNotifications() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    Page<Notification> notificationPage = new PageImpl<>(List.of(notification));
    when(notificationRepository.findAllByMemberIdAndIsReadFalse(member.getId(), pageable)).thenReturn(
        notificationPage);
    // when
    Page<NotificationDto> notifications = notificationService.getAllNotifications(member.getId(),
        pageable);
    // then
    assertNotNull(notifications);
    assertFalse(notifications.isEmpty());
    assertEquals(1, notifications.getTotalElements());
  }

  @Test
  @DisplayName("저장된 알림이 없는 경우 빈 배열을 반환한다.")
  void getAllNotifications_shouldReturnEmptyNotifications() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    Page<Notification> notificationPage = Page.empty();

    when(notificationRepository.findAllByMemberIdAndIsReadFalse(member.getId(), pageable)).thenReturn(
        notificationPage);
    // when
    Page<NotificationDto> notifications = notificationService.getAllNotifications(member.getId(),
        pageable);
    // then
    assertNotNull(notifications);
    assertTrue(notifications.isEmpty());
  }

  @Test
  @DisplayName("저장된 알림이 있고, 읽은 상태면 빈 배열을 반환한다.")
  void getAllNotifications_shouldReturnEmptyWhenAllNotificationsRead() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    Page<Notification> notificationPage = new PageImpl<>(List.of(readNotification));
    when(notificationRepository.findAllByMemberIdAndIsReadFalse(member.getId(), pageable)).thenReturn(
        notificationPage);
    // when
    Page<NotificationDto> notifications = notificationService.getAllNotifications(member.getId(),
        pageable);
    // then
    assertNotNull(notifications);
    assertFalse(notifications.isEmpty());
  }

  @Test
  @DisplayName("알림을 읽음 상태로 변경 요청이 오면 읽음 상태로 변경한다.")
  void updateNotification_shouldMarkAsRead() {
    // given
    Long notificationId = 1L;
    when(notificationRepository.findByIdAndMemberId(notificationId, member.getId())).thenReturn(
        Optional.of(notification));
    // when
    notificationService.updateNotification(notificationId, member.getId());
    // then
    assertTrue(notification.getIsRead());
  }

  @Test
  @DisplayName("회원의 알림이 없으면 예외가 발생한다.")
  void updateNotification_shouldThrowExceptionWhenNotificationNotFound() {
    // given
    Long notificationId = 1L;
    when(notificationRepository.findByIdAndMemberId(notificationId, member.getId())).thenReturn(
        Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> notificationService.updateNotification(notificationId, member.getId()));
    // then
    assertEquals(NOTIFICATION_NOT_FOUND, e.getErrorCode());
  }

  @Test
  @DisplayName("이미 읽은 상태의 알림의 읽음요청을 보내면 예외가 발생한다.")
  void updateNotification_shouldThrowExceptionWhenNotificationAlreadyRead() {
    // given
    Long notificationId = 1L;
    when(notificationRepository.findByIdAndMemberId(notificationId, member.getId())).thenReturn(
        Optional.of(readNotification));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> notificationService.updateNotification(notificationId, member.getId()));
    // then
    assertEquals(ALREADY_READ, e.getErrorCode());
  }
}