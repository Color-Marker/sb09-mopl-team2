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
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

  private static final Pattern WATCH_DESTINATION_PATTERN =
      Pattern.compile("^/sub/contents/([0-9a-fA-F\\-]{36})/watch$");

  private static final String KEY_DELIMITER = "::";

  private final WatchingSessionService watchingSessionService;

  // (웹소켓 세션 ID + 구독 ID) -> 시청 세션 정보
  // 구독 해제(UNSUBSCRIBE) 프레임에는 destination이 없고 구독 ID만 전달되므로 구독 시점에 매핑
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
      log.debug("watch 세션 대상 아닌 구독: destination={}", destination);
      return;
    }

    UUID contentId = UUID.fromString(matcher.group(1));
    Principal principal = accessor.getUser();
    if (principal == null) {
      log.warn("watch 구독 시 인증 정보 없음: destination={}", destination);
      return;
    }
    UUID userId = UUID.fromString(principal.getName());

    UUID watchingSessionId = watchingSessionService.join(contentId, userId);

    String key = subscriptionKey(accessor.getSessionId(), accessor.getSubscriptionId());
    sessionInfoMap.put(key, new SessionInfo(userId, contentId, watchingSessionId));
    log.debug("시청 세션 시작: userId={}, contentId={}, key={}", userId, contentId, key);
  }

  @EventListener
  public void handleUnsubscribe(SessionUnsubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String key = subscriptionKey(accessor.getSessionId(), accessor.getSubscriptionId());

    SessionInfo info = sessionInfoMap.remove(key);
    if (info == null) {
      return;
    }

    watchingSessionService.leave(info.watchingSessionId());
    log.debug("시청 세션 종료(구독 해제): userId={}, contentId={}", info.userId(), info.contentId());
  }

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionPrefix = accessor.getSessionId() + KEY_DELIMITER;

    sessionInfoMap.entrySet().removeIf(entry -> {
      if (!entry.getKey().startsWith(sessionPrefix)) {
        return false;
      }
      SessionInfo info = entry.getValue();
      watchingSessionService.leave(info.watchingSessionId());
      log.debug("시청 세션 종료(연결 종료): userId={}, contentId={}", info.userId(), info.contentId());
      return true;
    });
  }

  private String subscriptionKey(String sessionId, String subscriptionId) {
    return sessionId + KEY_DELIMITER + subscriptionId;
  }

  private record SessionInfo(UUID userId, UUID contentId, UUID watchingSessionId) {}
}
