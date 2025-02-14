package com.gogym.gympay.service;

import com.gogym.common.annotation.RedissonLock;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.entity.constant.TransferType;
import com.gogym.gympay.repository.GymPayRepository;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymPayService {

  private final MemberService memberService;
  private final GymPayHistoryService gymPayHistoryService;
  private final GymPayRepository gymPayRepository;

  @Transactional
  public void save(Long memberId) {
    Member member = memberService.findById(memberId);
    GymPay gymPay = new GymPay(0, member);

    gymPayRepository.save(gymPay);
  }

  @RedissonLock(key = "'gym-pay:' + #gymPay.id")
  public void deposit(GymPay gymPay, int amount, Long counterpartyId, Long postId, TransferType transferType) {
    if (gymPay == null) {
      throw new CustomException(ErrorCode.GYM_PAY_NOT_FOUND);
    }

    gymPay.deposit(amount);
    gymPayHistoryService.save(transferType, amount, gymPay.getBalance(), counterpartyId,
        postId, gymPay);
  }

  @RedissonLock(key = "'gym-pay:' + #gymPay.id")
  public void withdraw(GymPay gymPay, int amount, Long counterpartyId, Long postId, TransferType transferType) {
    if (gymPay == null) {
      throw new CustomException(ErrorCode.GYM_PAY_NOT_FOUND);
    }

    if (gymPay.getBalance() < amount) {
      throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
    }

    gymPay.withdraw(amount);
    gymPayHistoryService.save(transferType, amount, gymPay.getBalance(), counterpartyId,
        postId, gymPay);
  }
}

