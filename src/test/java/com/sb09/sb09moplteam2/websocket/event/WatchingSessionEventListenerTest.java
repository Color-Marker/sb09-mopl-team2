package com.sb09.sb09moplteam2.websocket.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.websocket.service.WatchingSessionService;
import java.security.Principal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@ExtendWith(MockitoExtension.class)
class WatchingSessionEventListenerTest {

  @Mock
  private WatchingSessionService watchingSessionService;

  @InjectMocks
  private WatchingSessionEventListener watchingSessionEventListener;

  private UUID contentId;
  private UUID userId;
  private UUID watchingSessionId;
  private String wsSessionId;

  @BeforeEach
  void setUp() {
    contentId = UUID.randomUUID();
    userId = UUID.randomUUID();
    watchingSessionId = UUID.randomUUID();
    wsSessionId = "ws-session-1";
  }

  private Message<byte[]> buildSubscribeMessage(String destination, UUID userId, String sessionId) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination(destination);
    accessor.setSessionId(sessionId);
    if (userId != null) {
      accessor.setUser((Principal) () -> userId.toString());
    }
    accessor.setLeaveMutable(true);
    return org.springframework.messaging.support.MessageBuilder
        .withPayload(new byte[0])
        .setHeaders(accessor)
        .build();
  }

  private Message<byte[]> buildDisconnectMessage(String sessionId) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
    accessor.setSessionId(sessionId);
    accessor.setLeaveMutable(true);
    return org.springframework.messaging.support.MessageBuilder
        .withPayload(new byte[0])
        .setHeaders(accessor)
        .build();
  }

  @Test
  void watch_destination_구독시_시청_세션을_생성한다() {
    String destination = "/sub/contents/" + contentId + "/watch";
    Message<byte[]> message = buildSubscribeMessage(destination, userId, wsSessionId);
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    given(watchingSessionService.join(contentId, userId)).willReturn(watchingSessionId);

    watchingSessionEventListener.handleSubscribe(event);

    verify(watchingSessionService).join(contentId, userId);
  }

  @Test
  void watch_destination이_아니면_무시한다() {
    String destination = "/sub/contents/" + contentId + "/chat";
    Message<byte[]> message = buildSubscribeMessage(destination, userId, wsSessionId);
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    watchingSessionEventListener.handleSubscribe(event);

    verify(watchingSessionService, never()).join(any(), any());
  }

  @Test
  void destination이_없으면_무시한다() {
    Message<byte[]> message = buildSubscribeMessage(null, userId, wsSessionId);
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    watchingSessionEventListener.handleSubscribe(event);

    verify(watchingSessionService, never()).join(any(), any());
  }

  @Test
  void 인증_정보가_없으면_세션을_생성하지_않는다() {
    String destination = "/sub/contents/" + contentId + "/watch";
    Message<byte[]> message = buildSubscribeMessage(destination, null, wsSessionId);
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    watchingSessionEventListener.handleSubscribe(event);

    verify(watchingSessionService, never()).join(any(), any());
  }

  @Test
  void 구독_이후_연결_종료시_시청_세션을_종료한다() {
    String destination = "/sub/contents/" + contentId + "/watch";
    Message<byte[]> subscribeMessage = buildSubscribeMessage(destination, userId, wsSessionId);
    SessionSubscribeEvent subscribeEvent = new SessionSubscribeEvent(this, subscribeMessage);

    given(watchingSessionService.join(contentId, userId)).willReturn(watchingSessionId);
    watchingSessionEventListener.handleSubscribe(subscribeEvent);

    Message<byte[]> disconnectMessage = buildDisconnectMessage(wsSessionId);
    SessionDisconnectEvent disconnectEvent =
        new SessionDisconnectEvent(this, disconnectMessage, wsSessionId, null);

    watchingSessionEventListener.handleDisconnect(disconnectEvent);

    verify(watchingSessionService).leave(watchingSessionId);
  }

  @Test
  void 구독_기록이_없는_세션의_연결_종료는_무시한다() {
    Message<byte[]> disconnectMessage = buildDisconnectMessage("unknown-session");
    SessionDisconnectEvent disconnectEvent =
        new SessionDisconnectEvent(this, disconnectMessage, "unknown-session", null);

    watchingSessionEventListener.handleDisconnect(disconnectEvent);

    verify(watchingSessionService, never()).leave(any());
  }
}
