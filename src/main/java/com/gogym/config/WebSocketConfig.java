package com.gogym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import com.gogym.chat.handler.StompHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  
  private final StompHandler stompHandler;
  
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // 메시지 브로커 설정
    config.enableSimpleBroker("/topic", "/queue"); // 클라이언트 구독 경로
    config.setApplicationDestinationPrefixes("/app"); // 클라이언트 송신 경로
  }
  
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws") // WebSocket 연결 엔드포인트
        .setAllowedOriginPatterns( // Origin 허용 설정
            "http://localhost:8080", // 테스트 환경
            "http://localhost:3000", // 로컬 클라이언트 환경
            "http://localhost:3001", // 로컬 클라이언트 환경
            "https://gogym-eight.vercel.app/" // 배포 클라이언트 환경
        )
        .withSockJS(); // 브라우저 호환성을 위한 SockJS 라이브러리 지원
  }
  
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // STOMP 메시지 처리에 StompChannelInterceptor 등록
    registration.interceptors(this.stompHandler);
  }
  
}
