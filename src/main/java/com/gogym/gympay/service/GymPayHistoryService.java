package com.gogym.gympay.service;

import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.entity.GymPayHistory;
import com.gogym.gympay.entity.constant.TransferType;
import com.gogym.gympay.repository.GymPayHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymPayHistoryService {

  private final GymPayHistoryRepository gymPayHistoryRepository;

  @Transactional
  public void save(TransferType transferType, int amount, int balance, Long counterpartyId,
      GymPay gymPay) {
    GymPayHistory gymPayHistory = GymPayHistory.builder()
        .transferType(transferType)
        .amount(amount)
        .balance(balance)
        .counterpartyId(counterpartyId)
        .gymPay(gymPay)
        .build();
    gymPayHistory.getGymPay().addHistory(gymPayHistory);

    gymPayHistoryRepository.save(gymPayHistory);
  }
}
