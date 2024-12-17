package com.gogym.gympay.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.dto.WebhookPayload;
import com.gogym.gympay.dto.request.PreRegisterRequest;
import com.gogym.gympay.dto.response.FailureResponse;
import com.gogym.gympay.dto.response.PreRegisterResponse;
import com.gogym.gympay.entity.Payment;
import com.gogym.gympay.entity.constant.Status;
import com.gogym.gympay.event.PaymentStatusChangedEvent;
import com.gogym.gympay.repository.PaymentRepository;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.util.RedisUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

  private final RedisUtil redisUtil;
  private final ApplicationEventPublisher eventPublisher;

  private final MemberService memberService;
  private final GymPayService gymPayService;
  private final PortOneService portOneService;

  private final PaymentRepository paymentRepository;

  public static final String PAYMENT_ID_PREFIX = "payment:";

  @Transactional
  public PreRegisterResponse save(Long memberId, PreRegisterRequest request) {
    String paymentId = generateId();

    portOneService.preRegisterPayment(paymentId, request.amount());
    redisUtil.saveHash(
        PAYMENT_ID_PREFIX + paymentId + ":pre-payment",
        Map.of(
            "member-id", String.valueOf(memberId),
            "amount", String.valueOf(request.amount())
        ),
        60 * 10
    );

    return new PreRegisterResponse(paymentId);
  }

  @Transactional
  public void processWebhook(WebhookPayload webhookPayload) {
    if (!webhookPayload.type().equals("Transaction.Paid") &&
        !webhookPayload.type().equals("Transaction.Failed")) {
      return;
    }

    String merchantId = webhookPayload.data().paymentId();
    var result = portOneService.getPaymentInfo(webhookPayload.data().transactionId());

    Map<Object, Object> prePayment = getPrePaymentData(merchantId);
    long preAmount = Long.parseLong((String) prePayment.get("amount"));
    verifyAmount(result.amount().paid(), preAmount);

    Long memberId = Long.parseLong((String) prePayment.get("member-id"));
    Member member = memberService.findById(memberId);
    Payment payment = result.toEntity(member);
    paymentRepository.save(payment);

    if (result.status().equals(Status.PAID))
    gymPayService.charge(member, result.amount().paid());

    publishPaymentStatusChangedEvent(payment.getId(), webhookPayload.type(),
        new FailureResponse(result.failure().reason(), result.failure().pgCode(),
            result.failure().message()));
  }

  private String generateId() {
    String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
    Random random = new Random();

    while (true) {
      int randomNumber = random.nextInt(1000000);
      String formattedNumber = String.format("%06d", randomNumber);

      boolean isAdded = redisUtil.addToSet(PAYMENT_ID_PREFIX + "ids:" + date, formattedNumber,
          60 * 60 * 24);
      if (isAdded) {
        return date + "-" + formattedNumber;
      }
    }
  }

  private void verifyAmount(long paidAmount, long expectedAmount) {
    if (paidAmount != expectedAmount) {
      throw new CustomException(ErrorCode.PAYMENT_MISMATCH);
    }
  }

  private Map<Object, Object> getPrePaymentData(String paymentId) {
    Map<Object, Object> prePayment = redisUtil.getHash(
        PAYMENT_ID_PREFIX + paymentId + ":pre-payment");
    if (prePayment == null || prePayment.isEmpty()) {
      throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
    }
    return prePayment;
  }

  private void publishPaymentStatusChangedEvent(String paymentId, String type,
      FailureResponse failureResponse) {
    eventPublisher.publishEvent(
        new PaymentStatusChangedEvent(paymentId, type, failureResponse));
  }
}
