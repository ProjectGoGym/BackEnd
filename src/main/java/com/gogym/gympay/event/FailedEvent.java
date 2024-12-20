package com.gogym.gympay.event;

public record FailedEvent(
    String paymentId,
    String failureReason,
    String sseEventName
) {

}