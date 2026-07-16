package com.sb09.sb09moplteam2.websocket.event;

import com.sb09.sb09moplteam2.websocket.service.WatchingSessionService;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

  private static final Pattern WATCH_DESTINATION_PATTERN =
      Pattern.compile("^/sub/contents/([0-9a-fA-F\\-]{36})/watch$");

  private final WatchingSessionService watchingSessionService;

  // webSocket session id -> (userId, contentId) 매핑용 (disconnect 시 조회)
  private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String destination = accessor.getDestination();
    if (destination == null) {
      return;
    }

    Matcher matcher = WATCH_DESTINATION_PATTERN.matcher(destination);
    if (!matcher.matches()) {
      return;
    }

    UUID contentId = UUID.fromString(matcher.group(1));
    Principal principal = accessor.getUser();
    if (principal == null) {
      log.warn("watch 구독 시 인증 정보 없음: destination={}", destination);
      return;
    }
    UUID userId = UUID.fromString(principal.getName());

    String webSocketSessionId = accessor.getSessionId();
    UUID watchingSessionId = watchingSessionService.join(contentId, userId);

    sessionInfoMap.put(webSocketSessionId, new SessionInfo(userId, contentId, watchingSessionId));
    log.debug("시청 세션 시작: userId={}, contentId={}, wsSessionId={}",
        userId, contentId, webSocketSessionId);
  }

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String webSocketSessionId = accessor.getSessionId();

    SessionInfo info = sessionInfoMap.remove(webSocketSessionId);
    if (info == null) {
      return;
    }

    watchingSessionService.leave(info.watchingSessionId());
    log.debug("시청 세션 종료: userId={}, contentId={}, wsSessionId={}",
        info.userId(), info.contentId(), webSocketSessionId);
  }

  private record SessionInfo(UUID userId, UUID contentId, UUID watchingSessionId) {}
}
