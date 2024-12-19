package com.gogym.gympay.dto.request;

import com.gogym.gympay.entity.SafePayment;
import com.gogym.gympay.entity.Transaction;
import com.gogym.gympay.entity.constant.RequesterRole;
import com.gogym.member.entity.Member;

public record SafePaymentRequest(Long responderId,
                                 int amount) {

  public SafePayment toEntity(Member requester, Member responder, Transaction transaction, RequesterRole requesterRole) {
    return SafePayment.builder()
        .requester(requester)
        .responder(responder)
        .amount(amount)
        .transaction(transaction)
        .requesterRole(requesterRole)
        .build();
  }
}
