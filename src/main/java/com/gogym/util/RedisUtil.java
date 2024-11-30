package com.gogym.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {

  private final StringRedisTemplate redisTemplate;

  public void save(String key, String value, long ttl) {
    redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttl));
  }

  public String get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public void delete(String key) {
    redisTemplate.delete(key);
  }
}