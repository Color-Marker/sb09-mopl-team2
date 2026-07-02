package com.sb09.sb09moplteam2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

  @Override
  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
    messages
        // CONNECT/DISCONNECT/HEARTBEAT는 인터셉터에서 처리
        .simpTypeMatchers(
            SimpMessageType.CONNECT,
            SimpMessageType.DISCONNECT,
            SimpMessageType.HEARTBEAT
        ).permitAll()
        // 메시지 전송 및 구독은 인증 필요
        .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).authenticated()
        .anyMessage().authenticated();
  }

  // SockJS 사용 시 CSRF 비활성화 (REST API와 CSRF 정책이 다름)
  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }
}
