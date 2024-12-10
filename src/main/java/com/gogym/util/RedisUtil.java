package com.gogym.util;

import java.time.Duration;
import java.util.List;
import java.util.Map;
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

  public void lpush(String key, String value) {
    redisTemplate.opsForList().leftPush(key, value);
  }

  public List<String> lrange(String key, long start, long end) {
    return redisTemplate.opsForList().range(key, start, end);
  }

  public boolean addToSet(String key, String value, long ttl) {
    Long result = redisTemplate.opsForSet().add(key, value);
    if (result == 0) {
      return false;
    }
    setTTL(key, ttl);
    return true;
  }

  public void setTTL(String key, long ttl) {
    if (redisTemplate.getExpire(key) == -1) {
      redisTemplate.expire(key, Duration.ofSeconds(ttl));
    }
  }

  public void saveHash(String key, Map<String, String> data, long ttl) {
    redisTemplate.opsForHash().putAll(key, data);
    redisTemplate.expire(key, Duration.ofSeconds(ttl));
  }

  public Map<Object, Object> getHash(String key) {
    return redisTemplate.opsForHash().entries(key);
  }
}
