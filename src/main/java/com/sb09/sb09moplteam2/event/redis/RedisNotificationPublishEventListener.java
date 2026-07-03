package com.sb09.sb09moplteam2.event.redis;

import com.sb09.sb09moplteam2.event.message.NotificationCreatedEvent;
import com.sb09.sb09moplteam2.event.message.NotificationDmEvent;
import com.sb09.sb09moplteam2.event.message.NotificationRoleEvent;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.sse.SseService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisNotificationPublishEventListener {

  private final SseService sseService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationRoleEvent event) {
    log.info(">>> NotificationRoleEvent 리스너 진입: {}", event);
    NotificationDto dto = event.getData();
    sseService.publishToRedis(Set.of(dto.receiverId()),"notifications",dto);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationDmEvent event) {
    log.info(">>> NotificationDmEvent 리스너 진입: {}", event);
    NotificationDto dto = event.getData();
    sseService.publishToRedis(Set.of(dto.receiverId()),"notifications",dto);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationCreatedEvent event) {
    log.info(">>> NotificationCreatedEvent 리스너 진입: {}", event);
    event.getData().forEach(dto ->
        sseService.publishToRedis(Set.of(dto.receiverId()), "notifications", dto)
    );
  }
}
