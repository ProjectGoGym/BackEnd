package com.gogym.gympay.service;

import com.gogym.common.annotation.RedissonLock;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.entity.constant.TransferType;
import com.gogym.gympay.event.GymPayBalanceChangedEvent;
import com.gogym.gympay.repository.GymPayRepository;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymPayService {

  private final ApplicationEventPublisher eventPublisher;

  private final MemberService memberService;
  private final GymPayRepository gymPayRepository;

  @Transactional
  public void save(Long memberId) {
    Member member = memberService.findById(memberId);
    GymPay gymPay = new GymPay(0, member);

    gymPayRepository.save(gymPay);
  }

  @RedissonLock(key = "'gym-pay:' + #gymPay.id")
  public void deposit(GymPay gymPay, int amount, Long counterpartyId) {
    if (gymPay == null) {
      throw new CustomException(ErrorCode.GYM_PAY_NOT_FOUND);
    }

    gymPay.deposit(amount);
    eventPublisher.publishEvent(new GymPayBalanceChangedEvent(
        TransferType.DEPOSIT, amount, gymPay.getBalance(), counterpartyId, gymPay));
  }

  @RedissonLock(key = "'gym-pay:' + #gymPay.id")
  public void withdraw(GymPay gymPay, int amount, Long counterpartyId) {
    if (gymPay == null) {
      throw new CustomException(ErrorCode.GYM_PAY_NOT_FOUND);
    }

    if (gymPay.getBalance() < amount) {
      throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
    }

    gymPay.withdraw(amount);
    eventPublisher.publishEvent(new GymPayBalanceChangedEvent(
        TransferType.WITHDRAWAL, amount, gymPay.getBalance(), counterpartyId, gymPay));
  }
}

