package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.config.jwt.JwtChannelInterceptor;
import com.sb09.sb09moplteam2.websocket.interceptor.StompLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final JwtChannelInterceptor jwtChannelInterceptor;
  private final StompLoggingInterceptor stompLoggingInterceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 클라이언트 구독 prefix: /sub (1:N 브로드캐스트)
    registry.enableSimpleBroker("/sub");
    // 클라이언트 → 서버 전송 prefix
    registry.setApplicationDestinationPrefixes("/pub");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompLoggingInterceptor, jwtChannelInterceptor);
  }
}
