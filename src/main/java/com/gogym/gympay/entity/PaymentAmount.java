package com.gogym.gympay.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class PaymentAmount {

  private Long total;

  @Column(name = "tax_free")
  private Long taxFree;

  private Long vat;

  private Long supply;

  private Long discount;

  private Long paid;

  private Long cancelled;

  @Column(name = "cancelled_tax_free")
  private Long cancelledTaxFree;
}
