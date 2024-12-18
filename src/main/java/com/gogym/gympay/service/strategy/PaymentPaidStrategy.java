package com.gogym.gympay.service.strategy;

import com.gogym.common.annotation.RedissonLock;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.dto.PaymentResult;
import com.gogym.gympay.entity.constant.TransferType;
import com.gogym.gympay.event.PaidEvent;
import com.gogym.gympay.service.GymPayHistoryService;
import com.gogym.member.entity.Member;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component("PAID")
@RequiredArgsConstructor
public class PaymentPaidStrategy implements PaymentProcessingStrategy {

  private final ApplicationEventPublisher eventPublisher;
  private final GymPayHistoryService gymPayHistoryService;

  @Override
  @RedissonLock(key = "'gym-pay:' + #member.gymPay.id")
  public void process(PaymentResult result, Map<Object, Object> preRegisteredData, Member member) {
    if (member.getGymPay() == null) {
      throw new CustomException(ErrorCode.GYM_PAY_NOT_FOUND);
    }
    member.getGymPay().deposit(result.amount().total());

    eventPublisher.publishEvent(new PaidEvent(result.id(), result.status()));
    gymPayHistoryService.save(TransferType.CHARGE, result.amount().total(), member.getGymPay().getBalance(), null, member.getGymPay());
  }
}
