package com.gogym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);

    // Key Serializer: String
    redisTemplate.setKeySerializer(new StringRedisSerializer());

    // Value Serializer: JSON
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

    // Hash Key Serializer: String
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());

    // Hash Value Serializer: JSON
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

}
