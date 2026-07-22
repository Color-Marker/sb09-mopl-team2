package com.sb09.sb09moplteam2.websocket.relay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis STOMP 브로드캐스트 채널 구독자.
 * 어느 인스턴스에서 발행됐든 수신하여 자기 인스턴스의 로컬 브로커로 방송한다.
 */
@Slf4j
@Component
public class StompBroadcastSubscriber implements MessageListener {

  private final GenericJackson2JsonRedisSerializer redisSerializer;
  private final SimpMessagingTemplate messagingTemplate;

  public StompBroadcastSubscriber(
      @Qualifier("redisSerializer") GenericJackson2JsonRedisSerializer redisSerializer,
      SimpMessagingTemplate messagingTemplate
  ) {
    this.redisSerializer = redisSerializer;
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      StompBroadcastMessage payload =
          (StompBroadcastMessage) redisSerializer.deserialize(message.getBody());
      if (payload == null) {
        return;
      }
      log.debug("STOMP 브로드캐스트 수신: destination={}", payload.destination());
      messagingTemplate.convertAndSend(payload.destination(), payload.payload());
    } catch (Exception e) {
      log.error("Redis STOMP 브로드캐스트 처리 실패", e);
    }
  }
}
