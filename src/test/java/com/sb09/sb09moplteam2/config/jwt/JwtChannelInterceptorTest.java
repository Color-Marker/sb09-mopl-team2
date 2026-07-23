package com.sb09.sb09moplteam2.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.sb09.sb09moplteam2.security.jwt.JwtProvider;
import com.sb09.sb09moplteam2.security.jwt.SessionBlacklistService;
import com.sb09.sb09moplteam2.user.entity.Role;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@ExtendWith(MockitoExtension.class)
class JwtChannelInterceptorTest {

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private SessionBlacklistService sessionBlacklistService;

  private JwtChannelInterceptor interceptor;

  private static final String TOKEN = "valid.jwt.token";

  private JwtChannelInterceptor interceptor() {
    return new JwtChannelInterceptor(jwtProvider, sessionBlacklistService);
  }

  private Message<byte[]> messageOf(StompHeaderAccessor accessor) {
    return org.springframework.messaging.support.MessageBuilder.createMessage(
        new byte[0], accessor.getMessageHeaders());
  }

  @Test
  @DisplayName("CONNECT 시 유효한 토큰이면 인증하고 세션ID를 세션 속성에 저장한다")
  void connect_유효토큰이면_인증하고_세션ID를_저장한다() {
    interceptor = interceptor();
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();

    given(jwtProvider.isValid(TOKEN)).willReturn(true);
    given(jwtProvider.getSessionId(TOKEN)).willReturn(sessionId);
    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(false);
    given(jwtProvider.getUserId(TOKEN)).willReturn(userId);
    given(jwtProvider.getRole(TOKEN)).willReturn(Role.USER);

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.setSessionAttributes(new HashMap<>());
    accessor.addNativeHeader("Authorization", "Bearer " + TOKEN);
    accessor.setLeaveMutable(true);

    interceptor.preSend(messageOf(accessor), null);

    assertThat(accessor.getUser()).isNotNull();
    assertThat(accessor.getSessionAttributes())
        .containsEntry(JwtChannelInterceptor.SESSION_ID_ATTRIBUTE, sessionId);
  }

  @Test
  @DisplayName("CONNECT 시 블랙리스트된 세션의 토큰이면 거부한다")
  void connect_블랙리스트_세션이면_거부한다() {
    interceptor = interceptor();
    UUID sessionId = UUID.randomUUID();

    given(jwtProvider.isValid(TOKEN)).willReturn(true);
    given(jwtProvider.getSessionId(TOKEN)).willReturn(sessionId);
    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(true);

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.setSessionAttributes(new HashMap<>());
    accessor.addNativeHeader("Authorization", "Bearer " + TOKEN);
    accessor.setLeaveMutable(true);

    Message<byte[]> message = messageOf(accessor);
    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("SEND 시 세션이 무효화되었으면 메시지를 차단한다")
  void send_세션_무효화되면_차단한다() {
    interceptor = interceptor();
    UUID sessionId = UUID.randomUUID();

    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(true);

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
    Map<String, Object> attrs = new HashMap<>();
    attrs.put(JwtChannelInterceptor.SESSION_ID_ATTRIBUTE, sessionId);
    accessor.setSessionAttributes(attrs);
    accessor.setLeaveMutable(true);

    Message<byte[]> message = messageOf(accessor);
    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("세션이 만료");
  }

  @Test
  @DisplayName("SEND 시 세션이 유효하면 메시지를 통과시킨다")
  void send_세션_유효하면_통과한다() {
    interceptor = interceptor();
    UUID sessionId = UUID.randomUUID();

    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(false);

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
    Map<String, Object> attrs = new HashMap<>();
    attrs.put(JwtChannelInterceptor.SESSION_ID_ATTRIBUTE, sessionId);
    accessor.setSessionAttributes(attrs);
    accessor.setLeaveMutable(true);

    Message<byte[]> result = (Message<byte[]>) interceptor.preSend(messageOf(accessor), null);

    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("하트비트 시 세션이 무효화되었으면 연결을 차단한다")
  void heartbeat_세션_무효화되면_차단한다() {
    interceptor = interceptor();
    UUID sessionId = UUID.randomUUID();

    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(true);

    StompHeaderAccessor accessor = StompHeaderAccessor
        .createForHeartbeat();
    Map<String, Object> attrs = new HashMap<>();
    attrs.put(JwtChannelInterceptor.SESSION_ID_ATTRIBUTE, sessionId);
    accessor.setSessionAttributes(attrs);
    accessor.setLeaveMutable(true);

    Message<byte[]> message = messageOf(accessor);
    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("하트비트 시 세션이 유효하면 통과시킨다")
  void heartbeat_세션_유효하면_통과한다() {
    interceptor = interceptor();
    UUID sessionId = UUID.randomUUID();

    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(false);

    StompHeaderAccessor accessor = StompHeaderAccessor.createForHeartbeat();
    Map<String, Object> attrs = new HashMap<>();
    attrs.put(JwtChannelInterceptor.SESSION_ID_ATTRIBUTE, sessionId);
    accessor.setSessionAttributes(attrs);
    accessor.setLeaveMutable(true);

    assertThat(interceptor.preSend(messageOf(accessor), null)).isNotNull();
  }

  @Test
  @DisplayName("SUBSCRIBE 시 세션이 무효화되었으면 구독을 차단한다")
  void subscribe_세션_무효화되면_차단한다() {
    interceptor = interceptor();
    UUID sessionId = UUID.randomUUID();

    given(sessionBlacklistService.isBlacklisted(sessionId)).willReturn(true);

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    Map<String, Object> attrs = new HashMap<>();
    attrs.put(JwtChannelInterceptor.SESSION_ID_ATTRIBUTE, sessionId);
    accessor.setSessionAttributes(attrs);
    accessor.setLeaveMutable(true);

    Message<byte[]> message = messageOf(accessor);
    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
