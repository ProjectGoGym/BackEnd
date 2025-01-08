package com.gogym.gympay.entity.constant;

import java.util.EnumSet;
import java.util.Set;

public enum SafePaymentStatus {

  PENDING_APPROVAL,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED,
  REJECTED;

  private Set<SafePaymentStatus> allowedTransitions;

  static {
    PENDING_APPROVAL.allowedTransitions = EnumSet.of(IN_PROGRESS, REJECTED, CANCELLED);
    IN_PROGRESS.allowedTransitions = EnumSet.of(COMPLETED, CANCELLED);
    COMPLETED.allowedTransitions = EnumSet.noneOf(SafePaymentStatus.class);
    CANCELLED.allowedTransitions = EnumSet.noneOf(SafePaymentStatus.class);
    REJECTED.allowedTransitions = EnumSet.noneOf(SafePaymentStatus.class);
  }

  public boolean canTransitionTo(SafePaymentStatus targetStatus) {
    return allowedTransitions.contains(targetStatus);
  }
}