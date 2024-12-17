package com.gogym.gympay.entity;

import com.gogym.gympay.entity.constant.PaymentStatus;
import com.gogym.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment {

  @Id
  @Column(unique = true)
  private String id;

  @Enumerated(EnumType.STRING)
  private PaymentStatus status; // 결제 상태 (예: PAID, FAILED)

  @Column(name = "transaction_id", nullable = false)
  private String transactionId; // 거래 고유 ID

  @Column(name = "merchant_id", nullable = false)
  private String merchantId; // 상점 ID

  @Column(name = "store_id", nullable = false)
  private String storeId; // 매장 ID

  @Column(name = "order_name")
  private String orderName; // 주문 이름

  private String currency; // 결제 통화

  private boolean isCulturalExpense; // 문화비 사용 여부

  @Column(name = "requested_at")
  private LocalDateTime requestedAt; // 결제 요청 시간

  @Column(name = "updated_at")
  private LocalDateTime updatedAt; // 결제 상태 업데이트 시간

  @Column(name = "status_changed_at")
  private LocalDateTime statusChangedAt; // 결제 상태 변경 시간

  @Column(name = "paid_at")
  private LocalDateTime paidAt; // 결제 완료 시간

  @Column(name = "pg_tx_id")
  private String pgTxId; // PG사 거래 ID

  @Column(name = "receipt_url")
  private String receiptUrl; // 영수증 URL

  @Column(name = "failed_at")
  private LocalDateTime failedAt;

  @Column(name = "failure_reason")
  private String failureReason;

  @Embedded
  @Column(columnDefinition = "TEXT")
  private PaymentMethod paymentMethod;

  @Embedded
  @Column(columnDefinition = "TEXT")
  private PaymentChannel paymentChannel;

  @Embedded
  @Column(columnDefinition = "TEXT")
  private Amount amount;

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Embeddable
  public static class PaymentMethod {

    @Column(name = "payment_method_type")
    private String paymentMethodType; // 결제 유형 (예: PaymentMethodCard)

    private String provider; // 결제 제공사 (예: NAVERPAY)

    @Embedded
    @Column(columnDefinition = "TEXT")
    private EasyPayMethod easyPayMethod; // EasyPay 결제 방식

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Embeddable
    public static class EasyPayMethod {

      @Column(name = "easy_pay_method_type")
      private String easyPayMethodType; // EasyPay 결제 유형

      @Embedded
      @Column(columnDefinition = "TEXT")
      private Card card; // 카드 정보

      @Column(name = "approval_number")
      private String approvalNumber; // 승인 번호

      @Embedded
      @Column(columnDefinition = "TEXT")
      private Installment installment; // 할부 정보

      @Column(name = "point_used")
      private boolean pointUsed; // 포인트 사용 여부

      @Getter
      @Builder
      @AllArgsConstructor
      @NoArgsConstructor(access = AccessLevel.PROTECTED)
      @Embeddable
      public static class Card {

        private String publisher; // 카드 발급사

        private String issuer; // 카드 발행사

        private String brand; // 카드 브랜드 (예: MASTER)

        @Column(name = "card_type")
        private String cardType; // 카드 종류 (예: DEBIT)

        @Column(name = "owner_type")
        private String ownerType; // 카드 소유자 유형 (예: PERSONAL)

        private String bin; // BIN 번호

        @Column(name = "card_name")
        private String cardName; // 카드 이름

        private String number; // 카드 번호
      }

      @Getter
      @Builder
      @AllArgsConstructor
      @NoArgsConstructor(access = AccessLevel.PROTECTED)
      @Embeddable
      public static class Installment {

        private int month; // 할부 개월 수

        @Column(name = "is_interest_free")
        private boolean isInterestFree; // 무이자 여부
      }
    }
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Embeddable
  public static class PaymentChannel {

    @Column(name = "channel_type")
    private String channelType; // 결제 채널 유형 (예: TEST)

    @Column(name = "channel_id")
    private String channelId; // 채널 ID

    @Column(name = "channel_key")
    private String channelKey; // 채널 키

    @Column(name = "channel_name")
    private String channelName; // 채널 이름

    @Column(name = "pg_provider")
    private String pgProvider; // PG 제공사 (예: INICIS_V2)

    @Column(name = "pg_merchant_id")
    private String pgMerchantId; // PG사 상점 ID
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Embeddable
  public static class Amount {

    private int total; // 총 금액

    private int taxFree; // 면세 금액

    private int vat; // 부가세

    private int supply; // 공급가액

    private int discount; // 할인 금액

    private int paid; // 결제된 금액

    private int cancelled; // 취소된 금액

    @Column(name = "cancelled_tax_free")
    private int cancelledTaxFree; // 취소된 면세 금액
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;
}