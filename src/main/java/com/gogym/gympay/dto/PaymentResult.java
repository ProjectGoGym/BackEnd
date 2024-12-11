package com.gogym.gympay.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gogym.gympay.entity.Payment;
import com.gogym.gympay.entity.PaymentAmount;
import com.gogym.gympay.entity.constant.PaymentMethodType;
import com.gogym.gympay.entity.constant.PgProvider;
import com.gogym.gympay.entity.constant.SelectedChannelType;
import com.gogym.gympay.entity.constant.Status;
import com.gogym.member.entity.Member;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResult(
    Status status,
    String id,
    String merchantId,
    String storeId,
    Channel channel,
    LocalDateTime requestedAt,
    LocalDateTime paidAt,
    LocalDateTime cancelledAt,
    LocalDateTime failedAt,
    Amount amount,
    Method method,
    Failure failure
) {

  public record Channel(
      SelectedChannelType type,
      PgProvider pgProvider,
      String pgMerchantId
  ) {

  }

  public record Amount(
      Long total,
      Long taxFree,
      Long vat,
      Long supply,
      Long paid,
      Long discount,
      Long cancelled,
      Long cancelledTaxFree
  ) {

  }

  public record Method(
      PaymentMethodType type
  ) {

  }

  public record Failure(
      String reason,
      String pgCode,
      String message
  ) {

  }

  public Payment toEntity(Member member) {
    return Payment.builder()
        .id(id)
        .merchantId(this.merchantId)
        .transactionId(this.id)
        .status(this.status)
        .storeId(this.storeId)
        .paymentMethodType(this.method.type)
        .selectedChannelType(this.channel.type)
        .pgProvider(this.channel.pgProvider)
        .pgMerchantId(this.channel.pgMerchantId)
        .requestedAt(this.requestedAt)
        .paidAt(this.paidAt)
        .cancelledAt(this.cancelledAt)
        .failedAt(this.failedAt)
        .reason(this.failure != null ? this.failure.reason : null)
        .failedPgCode(this.failure != null ? this.failure.pgCode : null)
        .failedPgMessage(this.failure != null ? this.failure.message : null)
        .paymentAmount(
            PaymentAmount.builder()
                .total(this.amount.total)
                .taxFree(this.amount.taxFree)
                .vat(this.amount.vat)
                .supply(this.amount.supply)
                .paid(this.amount.paid)
                .discount(this.amount.discount)
                .cancelled(this.amount.cancelled)
                .cancelledTaxFree(this.amount.cancelledTaxFree)
                .build()
        )
        .member(member)
        .build();
  }
}