package com.gogym.gympay.controller;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogym.common.annotation.LoginMemberId;
import com.gogym.gympay.dto.WebhookPayload;
import com.gogym.gympay.dto.request.PreRegisterRequest;
import com.gogym.gympay.dto.response.PreRegisterResponse;
import com.gogym.gympay.service.PaymentService;
import com.gogym.gympay.service.SSEService;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentService paymentService;
  private final SSEService sseService;
  private final WebhookVerifier webhookVerifier;
  private final ObjectMapper objectMapper;

  public static final String HEADER_WEBHOOK_ID = "Webhook-Id";
  public static final String HEADER_WEBHOOK_SIGNATURE = "Webhook-Signature";
  public static final String HEADER_WEBHOOK_TIMESTAMP = "Webhook-Timestamp";

  @PostMapping("/pre-register")
  public ResponseEntity<PreRegisterResponse> preRegister(@LoginMemberId Long memberId,
      @RequestBody PreRegisterRequest request) {
    var response = paymentService.save(memberId, request);

    return ResponseEntity.ok(response);
  }


  @GetMapping(value = "/sse/subscribe/{payment-id}", produces = TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@PathVariable("payment-id") String paymentId) {
    return sseService.subscribe(paymentId);
  }

  @PostMapping("/webhook")
  public ResponseEntity<Void> handleWebhook(
      @RequestHeader(HEADER_WEBHOOK_ID) String webhookId,
      @RequestHeader(HEADER_WEBHOOK_SIGNATURE) String webhookSignature,
      @RequestHeader(HEADER_WEBHOOK_TIMESTAMP) String webhookTimestamp,
      @RequestBody String payload) {

    try {
      webhookVerifier.verify(payload, webhookId, webhookSignature, webhookTimestamp);
      WebhookPayload webhookPayload = objectMapper.readValue(payload, WebhookPayload.class);

      paymentService.processWebhook(webhookPayload);

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}