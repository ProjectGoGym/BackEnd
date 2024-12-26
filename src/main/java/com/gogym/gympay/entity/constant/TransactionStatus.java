package com.gogym.gympay.entity.constant;

import java.util.EnumSet;
import java.util.Set;

public enum TransactionStatus {
  STARTED,
  COMPLETED,
  CANCELLED;

  private Set<TransactionStatus> allowedTransitions;

  static {
    STARTED.allowedTransitions = EnumSet.of(COMPLETED, CANCELLED);
    COMPLETED.allowedTransitions = EnumSet.noneOf(TransactionStatus.class);
    CANCELLED.allowedTransitions = EnumSet.of(STARTED);
  }

  public boolean canTransitionTo(TransactionStatus targetStatus) {
    return allowedTransitions.contains(targetStatus);
  }
}