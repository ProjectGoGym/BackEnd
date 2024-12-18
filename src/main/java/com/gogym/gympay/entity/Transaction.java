package com.gogym.gympay.entity;

import com.gogym.chat.entity.ChatRoom;
import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.constant.TransactionStatus;
import com.gogym.member.entity.Member;
import com.gogym.post.entity.Post;
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
import lombok.AllArgsConstructor;
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
    this.chatRoom.setTransaction(this);
  }
}
