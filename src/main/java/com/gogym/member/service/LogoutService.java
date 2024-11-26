package com.gogym.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogoutService {

  private final RedisTemplate<String, String> redisTemplate;
  private static final long TOKEN_EXPIRATION_TIME = 60 * 60 * 1000; // 1시간 (밀리초)

  public void logout(String token) {
    // Redis에 토큰 저장 및 만료 시간 설정
    redisTemplate.opsForValue().set(token, "logout", TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
  }
}
