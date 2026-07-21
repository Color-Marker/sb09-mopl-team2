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
import jakarta.annotation.PreDestroy;
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
  public void on(RoleUpdatedEvent event) {
    log.info(">>> RoleUpdatedEvent 리스너 진입: {}", event);
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(SubscribedPlaylistEvent event) {
    log.info(">>> SubscribedPlaylistEvent 리스너 진입: {}", event);
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(SubsPlaylistWorkEvent event) {
    log.info(">>> SubsPlaylistWorkEvent  리스너 진입: {}", event);
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(FollowUserWorkEvent event) {
    log.info(">>> FollowUserWorkEvent 리스너 진입: {}", event);
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(FollowedEvent event) {
    log.info(">>> FollowedEvent 리스너 진입: {}", event);
    sendToKafka(event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) { // 웹소켓이랑 같이 봐야 함.
    log.info(">>> MessageCreatedEvent 리스너 진입: {}", event);
    sendToKafka(event);
  }


  private <T> void sendToKafka(T event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      String topic = "mopl.".concat(event.getClass().getSimpleName());
      kafkaTemplate.send(topic, payload).whenComplete((result, ex) -> {
        if (ex != null) {
          log.error("Kafka 전송 실패: topic={}", topic, ex);
        } else {
          log.info("Kafka 전송 성공: topic={}, offset={}", topic, result.getRecordMetadata().offset());
        }
      });
    } catch (JsonProcessingException e) {
      log.error("직렬화 실패", e);
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  public void flushKafkaOnShutdown() {
    log.info(">>> 애플리케이션 종료 - Kafka producer flush 시작");
    kafkaTemplate.flush();
    log.info(">>> Kafka producer flush 완료");
  }

}
