package com.sb09.sb09moplteam2.websocket.event;

import com.sb09.sb09moplteam2.event.message.MessageCreatedEvent;
import com.sb09.sb09moplteam2.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCreatedEventListener {

  private final SseService sseService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MessageCreatedEvent event) {
    log.debug("MessageCreatedEvent 수신: receiverId={}", event.userId());
    sseService.publishToRedis(
        Set.of(event.userId()),
        "dm.created",           // 프론트에서 구독할 이벤트 이름
        event.messageDto()      // 실제 페이로드 (필드명은 MessageCreatedEvent 구조에 맞게)
    );
  }
}
