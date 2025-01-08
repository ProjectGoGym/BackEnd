package com.gogym.gympay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gogym.gympay.entity.Payment;
import com.gogym.gympay.entity.constant.PaymentStatus;
import com.gogym.member.entity.Member;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentResult(
    String status, // 결제 상태 (예: PAID, FAILED)
    String id, // 결제 고유 ID
    String transactionId, // 거래 고유 ID
    String merchantId, // 상점 ID
    String storeId, // 매장 ID
    PaymentMethod method, // 결제 방식 (예: 카드 결제, EasyPay)
    PaymentChannel channel, // 결제 채널 (예: PG사, 결제 방식)
    String version, // API 버전
    List<Webhook> webhooks, // 결제 후 처리된 웹훅 응답 리스트
    String requestedAt, // 결제 요청 시간
    String updatedAt, // 결제 상태 업데이트 시간
    String statusChangedAt, // 결제 상태 변경 시간
    String orderName, // 주문 이름
    Amount amount, // 결제 금액 관련 정보
    String currency, // 결제 통화
    Customer customer, // 결제 고객 정보
    String promotionId, // 프로모션 ID (있는 경우)
    boolean isCulturalExpense, // 문화비 사용 여부
    String paidAt, // 결제 완료 시간
    String pgTxId, // PG사 거래 ID
    String pgResponse, // PG사 응답 데이터
    String receiptUrl, // 영수증 URL
    String failedAt, // 결제 실패 시간 (실패한 경우)
    Failure failure // 결제 실패 사유
) {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PaymentMethod(
      String type, // 결제 유형 (예: PaymentMethodEasyPay)
      String provider, // 결제 제공사 (예: NAVERPAY)
      EasyPayMethod easyPayMethod // EasyPay 결제 방식
  ) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EasyPayMethod(
        String type, // EasyPay 결제 유형 (예: PaymentMethodCard)
        Card card, // 카드 정보
        String approvalNumber, // 승인 번호
        Installment installment, // 할부 정보
        boolean pointUsed // 포인트 사용 여부
    ) {

      @JsonInclude(JsonInclude.Include.NON_NULL)
      @JsonIgnoreProperties(ignoreUnknown = true)
      public record Card(
          String publisher, // 카드 발급사
          String issuer, // 카드 발행사
          String brand, // 카드 브랜드 (예: MASTER)
          String type, // 카드 종류 (예: DEBIT)
          String ownerType, // 카드 소유자 유형 (예: PERSONAL)
          String bin, // BIN 번호
          String name, // 카드 이름
          String number // 카드 번호
      ) {

      }

      @JsonInclude(JsonInclude.Include.NON_NULL)
      @JsonIgnoreProperties(ignoreUnknown = true)
      public record Installment(
          int month, // 할부 개월 수
          boolean isInterestFree // 무이자 여부
      ) {

      }
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PaymentChannel(
      String type, // 결제 채널 유형 (예: TEST)
      String id, // 채널 ID
      String key, // 채널 키
      String name, // 채널 이름
      String pgProvider, // PG 제공사 (예: INICIS_V2)
      String pgMerchantId // PG사 상점 ID
  ) {

  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Webhook(
      String paymentStatus, // 결제 상태 (예: FAILED)
      String id, // 웹훅 고유 ID
      String status, // 웹훅 상태
      String url, // 웹훅 URL
      boolean isAsync, // 비동기 여부
      int currentExecutionCount, // 실행 횟수
      Request request, // 요청 데이터
      Response response, // 응답 데이터
      String triggeredAt // 웹훅 트리거 시간
  ) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
        String header, // 요청 헤더
        String body, // 요청 본문
        String requestedAt // 요청 시간
    ) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
        String code, // 응답 코드
        String header, // 응답 헤더
        String body, // 응답 본문
        String respondedAt // 응답 시간
    ) {

    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Amount(
      int total, // 총 금액
      int taxFree, // 면세 금액
      int vat, // 부가세
      int supply, // 공급가액
      int discount, // 할인 금액
      int paid, // 결제된 금액
      int cancelled, // 취소된 금액
      int cancelledTaxFree // 취소된 면세 금액
  ) {

  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Customer(
      String id, // 고객 ID
      String name, // 고객 이름
      String email, // 고객 이메일
      String phoneNumber // 고객 전화번호
  ) {

  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Failure(
      String reason, // 실패 사유
      String pgCode, // PG사 실패코드
      String pgMessage // PG사 실패 메세지
  ) {

  }

  public Payment toEntity(Member member) {
    return Payment.builder()
        .id(id)
        .status(PaymentStatus.valueOf(this.status)) // 결제 상태 (PAID, FAILED 등)
        .transactionId(this.transactionId) // 거래 고유 ID
        .merchantId(this.merchantId) // 상점 ID
        .storeId(this.storeId) // 매장 ID
        .orderName(this.orderName) // 주문 이름
        .currency(this.currency) // 결제 통화
        .isCulturalExpense(this.isCulturalExpense) // 문화비 사용 여부
        .requestedAt(convertToKST(this.requestedAt)) // 결제 요청 시간
        .updatedAt(convertToKST(this.updatedAt)) // 결제 상태 업데이트 시간
        .statusChangedAt(convertToKST(this.statusChangedAt)) // 결제 상태 변경 시간
        .paidAt(this.paidAt != null ? convertToKST(this.paidAt) : null) // 결제 완료 시간
        .pgTxId(this.pgTxId) // PG사 거래 ID
        .receiptUrl(this.receiptUrl) // 영수증 URL
        .paymentMethod(Payment.PaymentMethod.builder()
            .paymentMethodType(this.method.type) // 결제 방식 (예: 카드, 간편결제 등)
            .provider(this.method.provider) // 결제 제공사
            .easyPayMethod(
                this.method.easyPayMethod != null ? Payment.PaymentMethod.EasyPayMethod.builder()
                    .easyPayMethodType(this.method.easyPayMethod.type) // EasyPay 결제 유형
                    .card(this.method.easyPayMethod.card != null
                        ? Payment.PaymentMethod.EasyPayMethod.Card.builder()
                        .publisher(this.method.easyPayMethod.card.publisher)
                        .issuer(this.method.easyPayMethod.card.issuer)
                        .brand(this.method.easyPayMethod.card.brand)
                        .cardType(this.method.easyPayMethod.card.type)
                        .ownerType(this.method.easyPayMethod.card.ownerType)
                        .bin(this.method.easyPayMethod.card.bin)
                        .cardName(this.method.easyPayMethod.card.name)
                        .number(this.method.easyPayMethod.card.number)
                        .build() : null)
                    .approvalNumber(this.method.easyPayMethod.approvalNumber) // 승인 번호
                    .installment(this.method.easyPayMethod.installment != null
                        ? Payment.PaymentMethod.EasyPayMethod.Installment.builder()
                        .month(this.method.easyPayMethod.installment.month) // 할부 개월 수
                        .isInterestFree(
                            this.method.easyPayMethod.installment.isInterestFree) // 무이자 여부
                        .build() : null)
                    .pointUsed(this.method.easyPayMethod.pointUsed) // 포인트 사용 여부
                    .build() : null)
            .build())
        .paymentChannel(Payment.PaymentChannel.builder()
            .channelType(this.channel.type) // 결제 채널 유형
            .channelId(this.channel.id) // 채널 ID
            .channelKey(this.channel.key) // 채널 키
            .channelName(this.channel.name) // 채널 이름
            .pgProvider(this.channel.pgProvider) // PG 제공사
            .pgMerchantId(this.channel.pgMerchantId) // PG사 상점 ID
            .build())
        .amount(Payment.Amount.builder()
            .total(this.amount.total) // 총 금액
            .taxFree(this.amount.taxFree) // 면세 금액
            .vat(this.amount.vat) // 부가세
            .supply(this.amount.supply) // 공급가액
            .discount(this.amount.discount) // 할인 금액
            .paid(this.amount.paid) // 결제된 금액
            .cancelled(this.amount.cancelled) // 취소된 금액
            .cancelledTaxFree(this.amount.cancelledTaxFree) // 취소된 면세 금액
            .build())
        .failedAt(convertToKST(this.failedAt))
        .failureReason(this.failure != null ? this.failure.reason : null)
        .member(member)
        .build();
  }

  private LocalDateTime convertToKST(String utcDateTime) {
    if (utcDateTime == null) {
      return null;
    }

    OffsetDateTime offsetDateTime = OffsetDateTime.parse(utcDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    return offsetDateTime.atZoneSameInstant(ZoneOffset.ofHours(9)).toLocalDateTime();
  }
}