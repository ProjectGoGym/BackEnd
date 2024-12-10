package com.gogym.gympay.event;

import com.gogym.gympay.dto.response.FailureResponse;

public record PaymentStatusChangedEvent(
    String paymentId,
    String type,
    FailureResponse failureResponse
) {

}