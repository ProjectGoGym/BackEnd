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
import com.gogym.member.service.MemberService;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.entity.Notification;
import com.gogym.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
  private MemberService memberService;

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

    member = Member.builder().build();
    ReflectionTestUtils.setField(member, "id", 1L);
    notificationDto = new NotificationDto(1L, ADD_WISHLIST_MY_POST, "test message", null);
    notification = Notification.of(member, notificationDto);
    readNotification = new Notification(member, ADD_WISHLIST_MY_POST, "test message", true);
  }

  private Map<Long, SseEmitter> emitters() {
    return (Map<Long, SseEmitter>) ReflectionTestUtils.getField(notificationService, "emitters");
  }

  @Test
  void 구독을_신청한_회원은_Map_에_등록되어_관리된다() {
    // given
    // when
    sseEmitter = notificationService.subscribe(member.getId());
    // then
    assertNotNull(sseEmitter);
    Map<Long, SseEmitter> emitters = emitters();
    assertTrue(emitters.containsKey(member.getId()));
  }

  @Test
  void 알림_생성_시_DB_에_저장된다() {
    // given
    // when
    notificationService.createNotification(member.getId(), notificationDto);
    // then
    verify(notificationRepository).save(any());
  }

  @Test
  void 저장된_알림이_있고_읽지_않은_경우_알림이_조회된다() {
    // given
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
  void 저장된_알림이_없는_경우_빈_배열을_반환한다() {
    // given
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
  void 저장된_알림이_있고_읽은_상태면_빈_배열을_반환한다() {
    // given
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
  void 알림을_읽음_상태로_변경_요청이_오면_읽음_상태로_변경한다() {
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
  void 회원의_알림이_없으면_예외가_발생한다() {
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
  void 이미_읽은_상태의_알림의_읽음요청을_보내면_예외가_발생한다() {
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