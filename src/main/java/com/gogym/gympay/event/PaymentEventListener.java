package com.gogym.gympay.event;

import com.gogym.gympay.service.SSEService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final SSEService sseService;

  @EventListener
  public void handlePaymentPaid(PaidEvent event) {
    sseService.sendUpdate(event.paymentId(), event.sseEventName());
  }

  @EventListener
  public void handlePaymentFailed(FailedEvent event) {
    sseService.sendUpdate(event.paymentId(), event.sseEventName(), event.failureReason());
  }
}