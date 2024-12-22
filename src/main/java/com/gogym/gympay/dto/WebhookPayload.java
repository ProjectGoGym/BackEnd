package com.gogym.gympay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookPayload(String type,
                             String timestamp,
                             DataPayload data) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record DataPayload(String transactionId,
                            String storeId,
                            String paymentId,
                            String cancellationId) {
  }
}