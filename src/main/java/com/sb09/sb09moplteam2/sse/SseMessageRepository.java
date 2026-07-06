package com.sb09.sb09moplteam2.sse;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private static final String KEY = "sse:message-queue";

  @Value("${sse.event-queue-capacity:100}")
  private int eventQueueCapacity;

  private final RedisTemplate<String, Object> redisTemplate;

  public SseMessageRepository(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public SseMessage save(SseMessage message){
    redisTemplate.opsForList().rightPush(KEY, message);
    redisTemplate.opsForList().trim(KEY, -eventQueueCapacity, -1);
    return message;
  }

  public List<SseMessage> findAllByEventIdAfterAndReceiverId(UUID eventId, UUID receiverId) {
    List<Object> raw = redisTemplate.opsForList().range(KEY, 0, -1);
    if (raw == null) {
      return List.of();
    }
    return raw.stream()
        .map(o -> (SseMessage) o)
        .dropWhile(data -> !data.eventId().equals(eventId))
        .skip(1)
        .filter(m -> m.isReceivable(receiverId))
        .toList();
  }
}
