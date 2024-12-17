package com.gogym.gympay.event;

import com.gogym.gympay.service.SSEService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final SSEService sseService;

  @TransactionalEventListener
  public void handlePaymentPaid(PaidEvent event) {
//    sseService.sendUpdate(event.paymentId(), event.sseEventName());
  }

  @TransactionalEventListener
  public void handlePaymentFailed(FailedEvent event) {
//    sseService.sendUpdate(event.paymentId(), event.sseEventName(), event.failureReason());
  }
}