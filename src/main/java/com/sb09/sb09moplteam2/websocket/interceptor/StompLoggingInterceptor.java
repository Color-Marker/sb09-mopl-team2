package com.sb09.sb09moplteam2.websocket.interceptor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * STOMP 프레임마다 traceId를 MDC에 심고, 연결 생명주기(CONNECT/SUBSCRIBE/UNSUBSCRIBE/DISCONNECT)를
 * INFO 레벨로 로깅한다. WebSocketConfig에서 다른 인터셉터보다 먼저 등록해야
 * 이후 인터셉터(JwtChannelInterceptor 등)의 로그에도 traceId가 함께 찍힌다.
 */
@Slf4j
@Component
public class StompLoggingInterceptor implements ChannelInterceptor {

  // webSocket session id -> traceId (CONNECT 시 발급, DISCONNECT 시 제거)
  private final Map<String, String> traceIdMap = new ConcurrentHashMap<>();

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    String wsSessionId = accessor.getSessionId();
    StompCommand command = accessor.getCommand();

    String traceId = command == StompCommand.CONNECT
        ? traceIdMap.computeIfAbsent(wsSessionId, id -> generateTraceId())
        : traceIdMap.getOrDefault(wsSessionId, generateTraceId());

    MDC.put("traceId", traceId);
    MDC.put("wsSessionId", wsSessionId == null ? "-" : wsSessionId);

    if (isLifecycleCommand(command)) {
      log.info("STOMP {} - destination={}", command, accessor.getDestination());
    }

    return message;
  }

  @Override
  public void afterSendCompletion(
      Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && accessor.getCommand() == StompCommand.DISCONNECT) {
      traceIdMap.remove(accessor.getSessionId());
    }

    // 스레드 재사용 시 다음 메시지로 MDC가 새는 것 방지
    MDC.clear();
  }

  private boolean isLifecycleCommand(StompCommand command) {
    return command == StompCommand.CONNECT
        || command == StompCommand.SUBSCRIBE
        || command == StompCommand.UNSUBSCRIBE
        || command == StompCommand.DISCONNECT;
  }

  private String generateTraceId() {
    return UUID.randomUUID().toString().substring(0, 8);
  }
}
