package com.sb09.sb09moplteam2.config.jwt;

import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";
  // CONNECT 시점의 세션 ID를 STOMP 세션 속성에 저장해 두고, 이후 프레임마다 무효화 여부를 재검증한다.
  static final String SESSION_ID_ATTRIBUTE = "jwtSessionId";

  private final JwtProvider jwtProvider;
  private final SessionBlacklistService sessionBlacklistService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    StompCommand command = accessor.getCommand();
    if (StompCommand.CONNECT.equals(command)) {
      authenticateConnect(accessor);
    } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)
        || isHeartbeat(accessor, command)) {
      // 연결 이후 로그아웃/권한 변경/잠금으로 세션이 무효화되면 기존 웹소켓 연결도 차단해야 한다.
      // (CONNECT는 최초 1회만 인증되므로 메시지마다 블랙리스트를 재확인)
      // 하트비트 포함: 메시지를 보내지 않는 유휴 연결도 로그아웃 시 하트비트 시점에 끊겨
      // Disconnect 이벤트가 발생하고 시청 세션 퇴장 처리가 정상 수행됨
      rejectIfSessionBlacklisted(accessor);
    }

    return message;
  }

  private void authenticateConnect(StompHeaderAccessor accessor) {
    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
      log.warn("WebSocket CONNECT 인증 실패 - Authorization 헤더 없음");
      throw new IllegalArgumentException("Authorization 헤더가 필요합니다.");
    }

    String token = authHeader.substring(BEARER_PREFIX.length());

    if (!jwtProvider.isValid(token)) {
      log.warn("WebSocket CONNECT 인증 실패 - 유효하지 않은 토큰");
      throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
    }

    UUID sessionId = jwtProvider.getSessionId(token);
    if (sessionId != null && sessionBlacklistService.isBlacklisted(sessionId)) {
      log.warn("WebSocket CONNECT 인증 실패 - 무효화된 세션의 토큰");
      throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
    }

    UUID userId = jwtProvider.getUserId(token);
    String role = jwtProvider.getRole(token).name();

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            userId,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

    accessor.setUser(authentication);

    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
    if (sessionAttributes != null && sessionId != null) {
      sessionAttributes.put(SESSION_ID_ATTRIBUTE, sessionId);
    }

    log.debug("WebSocket CONNECT 인증 성공: userId={}", userId);
  }

  private boolean isHeartbeat(StompHeaderAccessor accessor, StompCommand command) {
    return command == null && SimpMessageType.HEARTBEAT.equals(accessor.getMessageType());
  }

  private void rejectIfSessionBlacklisted(StompHeaderAccessor accessor) {
    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
    if (sessionAttributes == null) {
      return;
    }

    Object sessionId = sessionAttributes.get(SESSION_ID_ATTRIBUTE);
    if (sessionId instanceof UUID uuid && sessionBlacklistService.isBlacklisted(uuid)) {
      log.warn("WebSocket 메시지 차단 - 무효화된 세션: sessionId={}", uuid);
      throw new IllegalArgumentException("세션이 만료되었습니다. 다시 로그인해주세요.");
    }
  }
}
