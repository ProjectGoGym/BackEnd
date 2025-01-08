package com.gogym.gympay.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.constant.RequesterRole;
import com.gogym.gympay.entity.constant.SafePaymentStatus;
import com.gogym.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(name = "safe_payments")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafePayment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  private int amount;

  @Enumerated(EnumType.STRING)
  private SafePaymentStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "requester_id", nullable = false)
  private Member requester;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "responder_id", nullable = false)
  private Member responder;

  @Enumerated(EnumType.STRING)
  @Column(name = "requester_role")
  private RequesterRole requesterRole;

  public static SafePayment of(Transaction transaction, Member requester, Member responder, int amount, RequesterRole requesterRole) {
    return SafePayment.builder()
        .transaction(transaction)
        .requester(requester)
        .responder(responder)
        .amount(amount)
        .requesterRole(requesterRole)
        .status(SafePaymentStatus.PENDING_APPROVAL)
        .build();
  }

  public void approve() {
    changeStatus(SafePaymentStatus.IN_PROGRESS);
  }

  public void reject() {
    changeStatus(SafePaymentStatus.REJECTED);
  }

  public void complete() {
    changeStatus(SafePaymentStatus.COMPLETED);
  }

  public void cancel() {
    changeStatus(SafePaymentStatus.CANCELLED);
  }

  private void changeStatus(SafePaymentStatus targetStatus) {
    if (!status.canTransitionTo(targetStatus)) {
      throw new IllegalStateException(
          String.format("'%s' 상태에서는 '%s' 상태로 전환할 수 없습니다.", this.status, targetStatus)
      );
    }
    this.status = targetStatus;
  }

  public Member getSeller() {
    return (requesterRole == RequesterRole.SELLER) ? this.requester : this.responder;
  }

  public Member getBuyer() {
    return requesterRole == RequesterRole.BUYER ? this.requester : this.responder;
  }
}
