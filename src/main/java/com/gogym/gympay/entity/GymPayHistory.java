package com.gogym.gympay.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.constant.TransferType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "gym_pay_histories")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GymPayHistory extends BaseEntity {

  @Enumerated(EnumType.STRING)
  TransferType transferType;

  private int amount;

  private int balance;

  @Column(name = "counterparty_id")
  private Long counterpartyId;

  @Column(name = "post_id")
  private Long postId;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "gymPay_id", nullable = false)
  private GymPay gymPay;
}
