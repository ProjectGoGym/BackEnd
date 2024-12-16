package com.gogym.gympay.service;

import com.gogym.gympay.dto.PaymentResult;
import com.gogym.gympay.dto.TokenInfo;
import com.gogym.util.RedisUtil;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional(readOnly = true)
public class PortOneService {

  private final WebClient portOneClient;
  private final RedisUtil redisUtil;

  @Value("${port-one.secret}")
  private String secretKey;

  public static final String ACCESS_TOKEN_KEY = "portone:access-token";
  public static final String REFRESH_TOKEN_KEY = "portone:refresh-token";

  public PortOneService(@Qualifier("portOneClient") WebClient portOneClient, RedisUtil redisUtil) {
    this.portOneClient = portOneClient;
    this.redisUtil = redisUtil;
  }

  public String getAccessToken() {
    String accessToken = redisUtil.get(ACCESS_TOKEN_KEY);

    if (accessToken != null && !accessToken.isEmpty()) {
      return accessToken;
    }

    return refreshAccessToken();
  }

  private String refreshAccessToken() {
    String refreshToken = redisUtil.get(REFRESH_TOKEN_KEY);

    if (refreshToken == null || refreshToken.isEmpty()) {
      return signIn();
    }

    try {
      TokenInfo tokenInfo = portOneClient.post()
          .uri("/token/refresh")
          .bodyValue(Map.of("refreshToken", refreshToken))
          .retrieve()
          .bodyToMono(TokenInfo.class)
          .block();

      storeTokens(tokenInfo);

      return tokenInfo.accessToken();
    } catch (Exception e) {
      return signIn();
    }
  }

  private String signIn() {
    TokenInfo tokenInfo = portOneClient.post()
        .uri("/login/api-secret")
        .bodyValue(Map.of("apiSecret", secretKey))
        .retrieve()
        .bodyToMono(TokenInfo.class)
        .block();

    storeTokens(tokenInfo);

    return tokenInfo.accessToken();
  }

  private void storeTokens(TokenInfo tokenInfo) {

    redisUtil.save(ACCESS_TOKEN_KEY, tokenInfo.accessToken(), 60 * 60 * 24);
    redisUtil.save(REFRESH_TOKEN_KEY, tokenInfo.refreshToken(), 60 * 60 * 24 * 7);
  }

  public void preRegisterPayment(String paymentId, int amount) {
    portOneClient.post()
        .uri("/payments/" + paymentId + "/pre-register")
        .headers(headers -> headers.setBearerAuth(getAccessToken()))
        .bodyValue(Map.of("totalAmount", amount))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }

  public PaymentResult getPaymentInfo(String transactionId) {
    return portOneClient.get()
        .uri("/payments/" + transactionId)
        .headers(headers -> headers.setBearerAuth("PortOne " + secretKey))
        .retrieve()
        .bodyToMono(PaymentResult.class)
        .block();
  }
}