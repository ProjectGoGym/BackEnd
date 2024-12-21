package com.gogym.gympay.service.strategy;

import com.gogym.common.annotation.RedissonLock;
import com.gogym.gympay.dto.PaymentResult;
import com.gogym.gympay.event.PaidEvent;
import com.gogym.gympay.service.GymPayService;
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
  private final GymPayService gymPayService;

  @Override
  @RedissonLock(key = "'gym-pay:' + #member.gymPay.id")
  public void process(PaymentResult result, Map<Object, Object> preRegisteredData, Member member) {
    gymPayService.deposit(member.getGymPay(), result.amount().total(), null, null);

    eventPublisher.publishEvent(new PaidEvent(result.id(), result.status()));
  }
}
