package com.gogym.gympay.event.listener;

import com.gogym.gympay.event.StartTransactionEvent;
import com.gogym.gympay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventListener {

  private final TransactionService transactionService;

  @EventListener
  public void onStartTransaction(StartTransactionEvent event) {
    transactionService.save(event.chatRoom(), event.seller(), event.buyer());
  }
}
