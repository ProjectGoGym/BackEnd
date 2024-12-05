package com.gogym.notification.repository;

import com.gogym.notification.entity.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findAllByMemberIdAndIsReadFalse(Long memberId, Pageable pageable);

  Optional<Notification> findByIdAndMemberId(Long id, Long memberId);
}