package com.sb09.sb09moplteam2.websocket.relay;

import com.sb09.sb09moplteam2.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * STOMP 브로드캐스트를 Redis pub/sub 경유로 발행하는 릴레이.
 *
 * 인메모리 심플 브로커는 자기 인스턴스에 연결된 구독자에게만 방송하므로,
 * 다중 인스턴스 환경에서는 메시지를 Redis 채널로 발행하고
 * 모든 인스턴스가 이를 수신해 각자 로컬 브로커로 방송해야 한다. (SSE와 동일한 패턴)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompBroadcastRelay {

  private final RedisTemplate<String, Object> redisTemplate;

  public void broadcast(String destination, Object payload) {
    log.debug("STOMP 브로드캐스트 발행: destination={}", destination);
    redisTemplate.convertAndSend(
        RedisConfig.STOMP_CHANNEL,
        new StompBroadcastMessage(destination, payload)
    );
  }
}
