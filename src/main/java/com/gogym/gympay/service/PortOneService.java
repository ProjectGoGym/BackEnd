package com.gogym.gympay.service;

import com.gogym.gympay.dto.PaymentResult;
import com.gogym.gympay.dto.TokenInfo;
import com.gogym.util.RedisService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PortOneService {

  private final WebClient portOneClient;
  private final RedisService redisService;

  @Value("${port-one.secret}")
  private String secretKey;

  public static final String ACCESS_TOKEN_KEY = "portone:access-token";
  public static final String REFRESH_TOKEN_KEY = "portone:refresh-token";

  public PortOneService(@Qualifier("portOneClient") WebClient portOneClient,
      RedisService redisService) {
    this.portOneClient = portOneClient;
    this.redisService = redisService;
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
        .doOnError(e -> log.error(e.getMessage()))
        .block();
  }

  private String getAccessToken() {
    String accessToken = redisService.get(ACCESS_TOKEN_KEY);

    if (accessToken != null && !accessToken.isEmpty()) {
      return accessToken;
    }

    return refreshAccessToken();
  }

  private String refreshAccessToken() {
    String refreshToken = redisService.get(REFRESH_TOKEN_KEY);

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
    redisService.save(ACCESS_TOKEN_KEY, tokenInfo.accessToken(), 60 * 30);
    redisService.save(REFRESH_TOKEN_KEY, tokenInfo.refreshToken(), 60 * 60 * 24);
  }
}