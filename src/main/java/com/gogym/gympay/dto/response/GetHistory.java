package com.gogym.gympay.dto.response;

import com.gogym.gympay.entity.constant.TransferType;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record GetHistory(Long historyId,
                         int amount,
                         int balance,
                         TransferType transferType,
                         Long counterpartyId,
                         String counterpartyNickname,
                         Long postId,
                         String postTitle,
                         LocalDateTime createdAt) {

  @QueryProjection
  public GetHistory {
  }
}
