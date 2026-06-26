package com.sb09.sb09moplteam2.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.sb09moplteam2.event.message.FollowUserWorkEvent;
import com.sb09.sb09moplteam2.event.message.FollowedEvent;
import com.sb09.sb09moplteam2.event.message.MessageCreatedEvent;
import com.sb09.sb09moplteam2.event.message.NotificationCreatedEvent;
import com.sb09.sb09moplteam2.event.message.NotificationDmEvent;
import com.sb09.sb09moplteam2.event.message.NotificationRoleEvent;
import com.sb09.sb09moplteam2.event.message.RoleUpdatedEvent;
import com.sb09.sb09moplteam2.event.message.SubsPlaylistWorkEvent;
import com.sb09.sb09moplteam2.event.message.SubscribedPlaylistEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

  // eventListener에서 잡아서 kafkaListener로 던져주는 로직

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationCreatedEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationDmEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationRoleEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(SubscribedPlaylistEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(SubsPlaylistWorkEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(FollowUserWorkEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(FollowedEvent event) {
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) { // 웹소켓이랑 같이 봐야 함.
    sendToKafka(event);
  }


  private <T> void sendToKafka(T event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("mopl.".concat(event.getClass().getSimpleName()), payload);
    } catch (JsonProcessingException e) {
      log.error("Kafka에 이벤트를 보내는데 실패하였습니다.", e);
      throw new RuntimeException(e);
    }
  }

}
