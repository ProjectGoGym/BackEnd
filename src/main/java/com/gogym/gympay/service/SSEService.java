package com.gogym.gympay.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.dto.response.FailureResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SSEService {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(String paymentId) {
    SseEmitter emitter = new SseEmitter(60 * 1000L);
    emitters.put(paymentId, emitter);
    emitter.onCompletion(() -> emitters.remove(paymentId));
    emitter.onTimeout(() -> emitters.remove(paymentId));

    return emitter;
  }

  public void sendUpdate(String paymentId, String eventName, FailureResponse response) {
    if (!emitters.containsKey(paymentId)) {
      throw new CustomException(ErrorCode.SSE_SUBSCRIPTION_NOT_FOUND);
    }

    SseEmitter emitter = emitters.get(paymentId);
    try {
      SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
          .id(paymentId)
          .name(eventName)
          .reconnectTime(3000);

      if (response != null) {
        eventBuilder.data(response);
      }

      emitter.send(eventBuilder.build());
    } catch (IOException e) {
      throw new CustomException(ErrorCode.SSE_SEND_ERROR);
    }
  }
}