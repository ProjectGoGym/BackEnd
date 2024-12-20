package com.gogym.gympay.service.strategy;

import com.gogym.gympay.dto.PaymentResult;
import java.util.List;

public class PaymentResultTestUtil {

  public static PaymentResult createDefaultPaymentResult() {
    return new PaymentResult(
        "PAID",
        "paymentId123",
        "transactionId123",
        "merchantId123",
        "storeId123",
        new PaymentResult.PaymentMethod(
            "CARD",
            "VISA",
            new PaymentResult.PaymentMethod.EasyPayMethod(
                "CARD",
                new PaymentResult.PaymentMethod.EasyPayMethod.Card(
                    "삼성카드", "비씨카드", "VISA", "DEBIT",
                    "PERSONAL", "123456", "삼성카드", "1111-2222-3333-4444"
                ),
                "123456",
                new PaymentResult.PaymentMethod.EasyPayMethod.Installment(3, true),
                true
            )
        ),
        new PaymentResult.PaymentChannel("PG", "channel123", "key123", "channelName", "INICIS", "merchantPgId"),
        "1.0",
        List.of(new PaymentResult.Webhook("PAID", "webhookId", "SUCCESS", "http://example.com", true, 1,
            new PaymentResult.Webhook.Request("header", "body", "2024-12-16T10:15:30Z"),
            new PaymentResult.Webhook.Response("200", "header", "body", "2024-12-16T10:15:30Z"),
            "2024-12-16T10:15:30Z"
        )),
        "2024-12-16T10:15:30Z",
        "2024-12-16T10:20:30Z",
        "2024-12-16T10:25:30Z",
        "주문명",
        new PaymentResult.Amount(10000, 0, 1000, 9000, 1000, 9000, 0, 0),
        "KRW",
        new PaymentResult.Customer("customerId", "홍길동", "test@example.com", "010-1234-5678"),
        "promotionId123",
        true,
        "2024-12-16T10:25:30Z",
        "pgTxId123",
        "PG Response",
        "http://example.com/receipt",
        "2024-12-16T10:20:30Z",
        new PaymentResult.Failure("결제 실패 사유")
    );
  }
}