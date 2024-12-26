package com.gogym.gympay.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SSEService {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(String paymentId) throws IOException {
    SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
    emitters.put(paymentId, emitter);

    emitter.onCompletion(() -> emitters.remove(paymentId));
    emitter.onTimeout(() -> {
      emitters.remove(paymentId);
      emitter.completeWithError(new CustomException(ErrorCode.SSE_TIMEOUT));
    });

    emitter.onError(e -> {
      emitters.remove(paymentId);
      emitter.completeWithError(
          new CustomException(ErrorCode.SSE_SUBSCRIPTION_ERROR, e.getMessage()));
    });

    emitter.send(SseEmitter.event()
        .id(paymentId)
        .name("Init")
        .data("connected"));

    return emitter;
  }

  public void sendUpdate(String paymentId, String eventName, String failureReason) {
    if (!emitters.containsKey(paymentId)) {
      throw new CustomException(ErrorCode.SSE_SUBSCRIPTION_NOT_FOUND);
    }

    SseEmitter emitter = emitters.get(paymentId);
    try {
      SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
          .id(paymentId)
          .name(eventName)
          .reconnectTime(3000);

      if (failureReason != null) {
        eventBuilder.data(failureReason);
      } else {
        eventBuilder.data("No failure reason provided.");
      }

      emitter.send(eventBuilder.build());
    } catch (IOException e) {
      throw new CustomException(ErrorCode.SSE_SEND_ERROR);
    }
  }

  public void sendUpdate(String paymentId, String eventName) {
    if (!emitters.containsKey(paymentId)) {
      throw new CustomException(ErrorCode.SSE_SUBSCRIPTION_NOT_FOUND);
    }

    SseEmitter emitter = emitters.get(paymentId);
    try {
      emitter.send(SseEmitter.event()
          .id(paymentId)
          .name(eventName)
          .reconnectTime(3000));
    } catch (IOException e) {
      throw new CustomException(ErrorCode.SSE_SEND_ERROR);
    }
  }
}