package com.gogym.gympay.entity;

import static lombok.AccessLevel.PROTECTED;

import com.gogym.common.entity.BaseEntity;
import com.gogym.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "gym_pays")
@NoArgsConstructor(access = PROTECTED)
public class GymPay extends BaseEntity {

  private int balance;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  public GymPay(int balance, Member member) {
    this.balance = balance;
    this.member = member;
  }

  public void deposit(int amount) {
    this.balance += amount;
  }

  public void withdraw(int amount) {
    this.balance -= amount;
  }
}
