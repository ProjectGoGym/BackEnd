package com.gogym.gympay.service;

import com.gogym.gympay.dto.SignInRequestDto;
import com.gogym.gympay.dto.TokenInfo;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional(readOnly = true)
public class PortOneService {

  private final WebClient portOneClient;

  public PortOneService(@Qualifier("portOneClient") WebClient portOneClient) {
    this.portOneClient = portOneClient;
  }

  // TODO: 배포하면 환경변수 설정 필요
  private static final String SECRET_KEY = Dotenv.load().get("IAMPORT_SECRET_KEY");

  public TokenInfo getTokens() {
    var request = new SignInRequestDto(SECRET_KEY);

    return portOneClient.post() // 주입받은 WebClient를 사용
        .uri("/login/api-secret")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(TokenInfo.class)
        .block();
  }
}