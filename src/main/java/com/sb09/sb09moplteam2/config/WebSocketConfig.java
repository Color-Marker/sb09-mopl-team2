package com.sb09.sb09moplteam2.config;

import com.sb09.sb09moplteam2.config.jwt.JwtChannelInterceptor;
import com.sb09.sb09moplteam2.websocket.interceptor.StompLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
    // 하트비트 전용 스케줄러 (@Scheduled용 TaskScheduler와 분리하기 위해 빈으로 등록하지 않음)
    ThreadPoolTaskScheduler heartbeatScheduler = new ThreadPoolTaskScheduler();
    heartbeatScheduler.setPoolSize(1);
    heartbeatScheduler.setThreadNamePrefix("ws-heartbeat-");
    heartbeatScheduler.setDaemon(true);
    heartbeatScheduler.initialize();

    // 클라이언트 구독 prefix: /sub (1:N 브로드캐스트)
    // 하트비트 활성화: 클라이언트가 주기적으로 하트비트를 보내게 되어
    // 로그아웃(세션 무효화)된 유휴 연결을 JwtChannelInterceptor가 감지해 끊을 수 있음
    registry.enableSimpleBroker("/sub")
        .setHeartbeatValue(new long[]{10000, 10000})
        .setTaskScheduler(heartbeatScheduler);
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
