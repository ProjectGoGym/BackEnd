package com.gogym.gympay.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.gogym.gympay.entity.constant.PaymentMethodType;
import com.gogym.gympay.entity.constant.PgProvider;
import com.gogym.gympay.entity.constant.SelectedChannelType;
import com.gogym.gympay.entity.constant.Status;
import com.gogym.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "payments")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment {

  @Id
  @Column(unique = true)
  private String id; // 결제 건 id (=주문 번호)

  @Setter
  @Column(name = "transaction_id", nullable = false)
  private String transactionId; // 결제 건 포트원 채번 아이디 (=결제 고유번호)

  @Column(name = "merchant_id", nullable = false)
  private String merchantId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status; // 상태

  @Column(name = "store_id")
  private String storeId; // 상점 id

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method_type")
  private PaymentMethodType paymentMethodType; // 결제 수단

  @Column(name = "selected_channel_type", nullable = false)
  private SelectedChannelType selectedChannelType; // 채널 정보

  @Column(name = "pg_provider", nullable = false)
  private PgProvider pgProvider; // PG사 결제 모듈

  @Column(name = "pg_merchant_id", nullable = false)
  private String pgMerchantId; // PG사 고객사 식별 아이디

  @Column(name = "requested_at")
  private LocalDateTime requestedAt; // 요청 시간

  @Column(name = "paid_at")
  private LocalDateTime paidAt; // 결제 완료 시간

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt; // 결제 취소 시간

  @Column(name = "failed_at")
  private LocalDateTime failedAt; // 결제 실패 시간

  @Column(name = "failed_reason")
  private String reason; // 결제 실패 사유

  @Column(name = "failed_pg_code")
  private String failedPgCode; // PG사 실패 코드

  @Column(name = "failed_pg_message")
  private String failedPgMessage; // PG사 실패 메세지

  @Embedded
  private PaymentAmount paymentAmount;

  @OneToOne(fetch = LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;
}