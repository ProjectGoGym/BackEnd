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
  public void handlePaymentStatusChanged(PaymentStatusChangedEvent event) {
    switch (event.type()) {
      case "transaction.paid" ->
          sseService.sendUpdate(event.paymentId(), "Transaction Paid", null);
      case "transaction.failed" -> {
        sseService.sendUpdate(event.paymentId(), "Transaction Failed", event.failureResponse());
      }
    }
  }
}