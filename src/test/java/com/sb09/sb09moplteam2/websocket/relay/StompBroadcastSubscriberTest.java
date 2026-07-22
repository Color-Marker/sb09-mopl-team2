package com.sb09.sb09moplteam2.websocket.relay;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class StompBroadcastSubscriberTest {

  @Mock
  private GenericJackson2JsonRedisSerializer redisSerializer;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @Mock
  private Message message;

  private StompBroadcastSubscriber subscriber;

  @BeforeEach
  void setUp() {
    subscriber = new StompBroadcastSubscriber(redisSerializer, messagingTemplate);
  }

  @Test
  @DisplayName("수신한 메시지의 destination으로 로컬 브로커에 방송한다")
  void onMessage_로컬_브로커로_방송한다() {
    byte[] body = new byte[]{1};
    given(message.getBody()).willReturn(body);
    given(redisSerializer.deserialize(body))
        .willReturn(new StompBroadcastMessage("/sub/contents/1/chat", "payload"));

    subscriber.onMessage(message, null);

    then(messagingTemplate).should()
        .convertAndSend("/sub/contents/1/chat", (Object) "payload");
  }

  @Test
  @DisplayName("역직렬화 실패 시 예외를 전파하지 않고 방송하지 않는다")
  void onMessage_역직렬화_실패시_방송하지_않는다() {
    byte[] body = new byte[]{1};
    given(message.getBody()).willReturn(body);
    given(redisSerializer.deserialize(body)).willThrow(new IllegalStateException("역직렬화 실패"));

    subscriber.onMessage(message, null);

    then(messagingTemplate).should(never()).convertAndSend(anyString(), any(Object.class));
  }
}
