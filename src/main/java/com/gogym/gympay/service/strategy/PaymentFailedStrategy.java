package com.gogym.gympay.service.strategy;

import com.gogym.gympay.dto.PaymentResult;
import com.gogym.gympay.event.FailedEvent;
import com.gogym.member.entity.Member;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("FAILED")
@RequiredArgsConstructor
public class PaymentFailedStrategy implements PaymentProcessingStrategy {

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void process(PaymentResult result, Map<Object, Object> preRegisteredData, Member member) {
//    eventPublisher.publishEvent(
//        new FailedEvent(result.id(), result.status(), result.failure().reason()));
  }
}
