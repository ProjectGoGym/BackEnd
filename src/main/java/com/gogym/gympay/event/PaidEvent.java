package com.gogym.gympay.event;

public record PaidEvent(
    String paymentId,
    String sseEventName
) {
}