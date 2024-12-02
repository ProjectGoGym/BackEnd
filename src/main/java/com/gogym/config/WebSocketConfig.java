package com.gogym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // 클라이언트가 메시지를 받을 경로(prefix)
    config.enableSimpleBroker("/topic", "/queue");
    // 클라이언트가 메시지를 보낼 경로(prefix)
    config.setApplicationDestinationPrefixes("/app");
  }
  
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket 연결 endpoint 설정(SockJS 사용)
    registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:3000").withSockJS();
  }
  
}
