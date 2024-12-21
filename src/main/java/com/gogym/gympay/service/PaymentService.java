package com.gogym.gympay.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.dto.PaymentResult;
import com.gogym.gympay.dto.WebhookPayload;
import com.gogym.gympay.dto.request.PreRegisterRequest;
import com.gogym.gympay.dto.response.PreRegisterResponse;
import com.gogym.gympay.entity.Payment;
import com.gogym.gympay.repository.PaymentRepository;
import com.gogym.gympay.service.strategy.PaymentProcessingStrategy;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.util.RedisService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

  private final Map<String, PaymentProcessingStrategy> paymentProcessingStrategyMap;

  private final RedisService redisService;

  private final MemberService memberService;
  private final PortOneService portOneService;

  private final PaymentRepository paymentRepository;

  public static final String PAYMENT_ID_PREFIX = "payment:";

  @Transactional
  public PreRegisterResponse save(Long memberId, PreRegisterRequest request) {
    Member member = memberService.findById(memberId);
    String paymentId = generateId();

    portOneService.preRegisterPayment(paymentId, request.amount());
    Map<String, String> preRegisteredPayment = new HashMap<>();
    preRegisteredPayment.put("member-email", member.getEmail());
    preRegisteredPayment.put("amount", String.valueOf(request.amount()));

    redisService.saveHash(PAYMENT_ID_PREFIX + paymentId, preRegisteredPayment, 60 * 5);

    return new PreRegisterResponse(paymentId);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void processWebhook(WebhookPayload webhookPayload) {
    var paymentResult = portOneService.getPaymentInfo(webhookPayload.data().paymentId());

    Map<Object, Object> preRegisteredData = getPreRegisteredData(paymentResult.id());
    Member member = memberService.findByEmail(paymentResult.customer().email());

    verifyPaymentDetails(preRegisteredData, paymentResult);

    if (paymentResult.status().equals("PAID") || paymentResult.status().equals("FAILED")) {
      PaymentProcessingStrategy strategy = paymentProcessingStrategyMap.get(paymentResult.status());
      strategy.process(paymentResult, preRegisteredData, member);
      Payment payment = paymentResult.toEntity(member);
      paymentRepository.save(payment);
    }
  }

  private String generateId() {
    String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
    String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
    String timestamp = String.valueOf(System.currentTimeMillis());

    return date + "-" + uuid + "-" + timestamp;
  }

  private Map<Object, Object> getPreRegisteredData(String paymentId) {
    Map<Object, Object> prePayment = redisService.getHash(
        PAYMENT_ID_PREFIX + paymentId);

    if (prePayment == null || prePayment.isEmpty()) {
      throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
    }
    return prePayment;
  }

  private void verifyPaymentDetails(Map<Object, Object> preRegisteredData, PaymentResult result) {
    int preRegisteredAmount = Integer.parseInt((String) preRegisteredData.get("amount"));
    int actualAmount = result.amount().paid();

    if (preRegisteredAmount != actualAmount) {
      log.error("결제 금액 불일치: 사전 등록 결제 금액 {}, 실 결제 금액 {}", preRegisteredAmount, actualAmount);
      throw new CustomException(ErrorCode.PAYMENT_MISMATCH);
    }

    String preRegisteredEmail = (String) preRegisteredData.get("member-email");
    String actualEmail = result.customer().email();

    if (!preRegisteredEmail.equals(actualEmail)) {
      log.error("이메일 불일치: 사전 등록 회원 이메일 {}, 실 결제 회원 이메일 {}", preRegisteredEmail, actualEmail);
      throw new CustomException(ErrorCode.PAYMENT_MISMATCH);
    }
  }
}
