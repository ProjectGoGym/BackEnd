package com.gogym.gympay.entity;

import static lombok.AccessLevel.PROTECTED;

import com.gogym.common.entity.BaseEntity;
import com.gogym.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "gym_pays")
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class GymPay extends BaseEntity {

  private long balance;

  @OneToOne(mappedBy = "gymPay")
  private Member member;

  public void charge(long amount) {
    this.balance += amount;
  }
}
