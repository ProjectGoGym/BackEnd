package com.gogym.gympay.service;

import com.gogym.gympay.dto.response.GetHistory;
import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.entity.GymPayHistory;
import com.gogym.gympay.entity.constant.TransferType;
import com.gogym.gympay.repository.GymPayHistoryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymPayHistoryService {

  private final GymPayHistoryRepository gymPayHistoryRepository;

  @Transactional
  public void save(TransferType transferType, int amount, int balance, Long counterpartyId,
      Long postId, GymPay gymPay) {
    GymPayHistory gymPayHistory = GymPayHistory.builder()
        .transferType(transferType)
        .amount(amount)
        .balance(balance)
        .counterpartyId(counterpartyId)
        .gymPay(gymPay)
        .postId(postId)
        .build();

    gymPayHistoryRepository.save(gymPayHistory);
  }

  public Page<GetHistory> getHistories(Long memberId, LocalDateTime startDate,
      LocalDateTime endDate, Pageable pageable) {
    return gymPayHistoryRepository.getAllHistoriesByGymPayIdAndPeriod(memberId, startDate, endDate,
        pageable);
  }
}
