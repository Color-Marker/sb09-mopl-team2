package com.sb09.sb09moplteam2.redis;

import com.sb09.sb09moplteam2.sse.SseMessage;
import com.sb09.sb09moplteam2.sse.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisSubscriber implements MessageListener {

  private final GenericJackson2JsonRedisSerializer redisSerializer;
  private final SseService sseService;

  public RedisSubscriber(
      @Qualifier("redisSerializer") GenericJackson2JsonRedisSerializer redisSerializer,
      SseService sseService
  ){
    this.redisSerializer = redisSerializer;
    this.sseService = sseService;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    log.info(">>> RedisSubscriber 작업. channel = {}", new String(message.getChannel()));
    try {
      SseMessage payload = (SseMessage) redisSerializer.deserialize(message.getBody());
      log.info(">>> 전송 작업 실시: {}", payload);
      sseService.send(payload);
    } catch (Exception e) {
      log.error("Redis SSE 메시지 처리 실패", e);
    }
  }
}
