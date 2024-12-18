package com.gogym.chat.handler;

import java.security.Principal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

/**
 * STOMP 메시지의 전처리를 담당하는 핸들러.
 * WebSocket 연결 시 JWT 토큰을 검증하고 사용자 정보를 세션에 저장합니다.
 */
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
  
  private final JwtTokenProvider jwtTokenProvider;
  
  /**
   * STOMP 메시지를 전처리하는 메서드.
   * 
   * @param message 클라이언트가 전송한 메시지
   * @param channel 메시지가 전달될 채널
   * @return 처리된 메시지
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    // STOMP 메시지 헤더 접근자 생성
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    
    // STOMP 메시지 명령이 CONNECT인 경우 -> WebSocket 초기 연결
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = accessor.getFirstNativeHeader("Authorization");
      
      // 헤더가 없거나 잘못된 경우
      if (token == null || !token.startsWith("Bearer ")) {
        throw new CustomException(ErrorCode.WEBSOCKET_UNAUTHORIZED);
      }
      
      token = token.substring(7); // "Bearer " 제거
      
      // 토큰 검증이 실패한 경우
      if (!this.jwtTokenProvider.validateToken(token)) {
        throw new CustomException(ErrorCode.WEBSOCKET_UNAUTHORIZED);
      }
      
      // 사용자 ID 추출
      Long memberId = this.jwtTokenProvider.extractMemberId(token);
      
      // 인증된 사용자 정보를 나타내는 Principal 객체 생성
      Principal principal = () -> String.valueOf(memberId);
      
      // STOMP 세션에 Principal 저장
      accessor.getSessionAttributes().put("principal", principal);
      
      // STOMP 메시지의 사용자 정보 설정
      accessor.setUser(principal);
    }
    
    return message;
  }

}
