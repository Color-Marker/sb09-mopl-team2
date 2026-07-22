package com.sb09.sb09moplteam2.websocket.relay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.sb09.sb09moplteam2.config.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class StompBroadcastRelayTest {

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @InjectMocks
  private StompBroadcastRelay stompBroadcastRelay;

  @Test
  @DisplayName("브로드캐스트 시 destination과 payload를 담아 Redis STOMP 채널로 발행한다")
  void broadcast_redis_채널로_발행한다() {
    String destination = "/sub/contents/123/chat";
    String payload = "chat-message";

    stompBroadcastRelay.broadcast(destination, payload);

    ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
    then(redisTemplate).should()
        .convertAndSend(eq(RedisConfig.STOMP_CHANNEL), messageCaptor.capture());

    StompBroadcastMessage sent = (StompBroadcastMessage) messageCaptor.getValue();
    assertThat(sent.destination()).isEqualTo(destination);
    assertThat(sent.payload()).isEqualTo(payload);
  }
}
