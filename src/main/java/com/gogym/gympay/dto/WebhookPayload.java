package com.gogym.gympay.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebhookPayload(String type,
                             String timestamp,
                             DataPayload data) {

  public record DataPayload(String transactionId,
                            String paymentId,
                            String cancellationId) {
  }
}