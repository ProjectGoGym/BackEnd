package com.gogym.gympay.event.listener;


import com.gogym.gympay.event.GymPayBalanceChangedEvent;
import com.gogym.gympay.service.GymPayHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GymPayEventListener {

  private final GymPayHistoryService gymPayHistoryService;

  @TransactionalEventListener
  public void saveHistory(GymPayBalanceChangedEvent event) {
    gymPayHistoryService.save(event.transferType(), event.amount(), event.balance(), event.counterpartyId(), event.gymPay());
  }
}
