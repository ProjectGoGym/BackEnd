package com.gogym.gympay.entity;

import com.gogym.chat.entity.ChatRoom;
import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.constant.TransactionStatus;
import com.gogym.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chatRoom_id", nullable = false)
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private Member seller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id", nullable = false)
  private Member buyer;

  @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<SafePayment> safePayments = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  private TransactionStatus status;

  @Setter
  @Column(name = "meeting_at")
  private LocalDateTime meetingAt;

  public Transaction(ChatRoom chatRoom, Member seller, Member buyer) {
    this.chatRoom = chatRoom;
    this.seller = seller;
    this.buyer = buyer;
    this.status = TransactionStatus.STARTED;
  }

  public void start() {
    changeStatus(TransactionStatus.STARTED);
  }

  public void complete() {
    changeStatus(TransactionStatus.COMPLETED);
  }

  public void cancel() {
    changeStatus(TransactionStatus.CANCELLED);
  }

  private void changeStatus(TransactionStatus targetStatus) {
    if (!status.canTransitionTo(targetStatus)) {
      throw new IllegalStateException(
          String.format("'%s' 상태에서는 '%s' 상태로 전환할 수 없습니다.", this.status, targetStatus)
      );
    }
    this.status = targetStatus;
  }
}
