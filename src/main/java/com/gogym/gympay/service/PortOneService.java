package com.gogym.gympay.service;

import com.gogym.gympay.dto.SignInRequestDto;
import com.gogym.gympay.dto.TokenInfo;
import com.gogym.util.RedisUtil;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Transactional(readOnly = true)
public class PortOneService {

  private final WebClient portOneClient;
  private final RedisUtil redisUtil;

  public static final String REDIS_KEY = "PORTONE_REFRESH_TOKEN";

  public PortOneService(@Qualifier("portOneClient") WebClient portOneClient, RedisUtil redisUtil) {
    this.portOneClient = portOneClient;
    this.redisUtil = redisUtil;
  }

  // TODO: 배포하면 환경변수 설정 필요
  private static final String SECRET_KEY = Dotenv.load().get("PORTONE_SECRET_KEY");

  public String signIn() {
    var request = new SignInRequestDto(SECRET_KEY);

    TokenInfo tokenInfo = portOneClient.post()
        .uri("/login/api-secret")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(TokenInfo.class)
        .block();
    redisUtil.save(REDIS_KEY, tokenInfo.refreshToken(), 60 * 60 * 24 * 7);

    return tokenInfo.accessToken();
  }
}