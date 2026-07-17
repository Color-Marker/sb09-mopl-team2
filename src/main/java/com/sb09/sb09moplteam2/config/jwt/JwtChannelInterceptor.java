package com.sb09.sb09moplteam2.config.jwt;

import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;
  private final SessionBlacklistService sessionBlacklistService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    // CONNECT 프레임에서만 JWT 인증 처리
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
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
      log.debug("WebSocket CONNECT 인증 성공: userId={}", userId);
    }

    return message;
  }
}
