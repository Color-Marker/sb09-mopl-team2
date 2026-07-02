package com.sb09.sb09moplteam2.sse;

import com.sb09.sb09moplteam2.config.RedisConfig;
import com.sb09.sb09moplteam2.redis.RedisPubSubMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  @Value("${sse.timeout}")
  private long timeout;

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  public SseEmitter connect(UUID receiverId, UUID lastEventId){
    SseEmitter sseEmitter = new SseEmitter(timeout);

    // 연결 정상 종료(onCompletion), 타임아웃(onTimeout), 에러 발생(onError) 시 콜백 등록
    // 해당 emitter 삭제해서 이상한 전송 방지
    sseEmitter.onCompletion(() -> {
      log.debug("sse 정상 종료됌");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });
    sseEmitter.onTimeout(() -> {
      log.debug("sse 타임 아웃됌");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });
    sseEmitter.onError((ex) -> {
      log.debug("sse 에러 발생");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });

    sseEmitterRepository.save(receiverId, sseEmitter);

    // lastEventId가 존재하는 경우 - 재연결 상황
    // 해당 이벤트 이후 쌓인 메시지 다시 전송.
    Optional.ofNullable(lastEventId)
        .ifPresentOrElse(
            id -> {
              sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, receiverId)
                  .forEach(sseMessage -> {
                    try {
                      sseEmitter.send(sseMessage.toEvent());
                    } catch (IOException e) {
                      log.error(e.getMessage(), e);
                    }
                  });
            },
            () -> {
              ping(sseEmitter);
            }
        );

    return sseEmitter;
  }

  // 메시지 레포에 저장하고 redis에 publish
  public void publishToRedis(Collection<UUID> receiverIds, String eventName, Object data) {
    SseMessage message = sseMessageRepository.save(SseMessage.create(receiverIds, eventName, data));

    redisTemplate.convertAndSend(
        RedisConfig.SSE_CHANNEL,
        RedisPubSubMessage.from(message)
    );
  }

  // redis 구독자가 받은 메시지 보고 emitter 찾아서 send 처리
  public void send(RedisPubSubMessage payload){
    Set<DataWithMediaType> event = SseEmitter.event()
        .id(payload.eventId().toString())
        .name(payload.eventName())
        .data(payload.eventData())
        .build();

    sseEmitterRepository.findAllByReceiverIdsIn(payload.receiverIds())
        .forEach(sseEmitter -> {
          try {
            sseEmitter.send(event);
          } catch (IOException e) {
            log.error(e.getMessage(), e);
          }
        });
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    sseEmitterRepository.findAll()
        .stream().filter(sseEmitter -> !ping(sseEmitter))
        .forEach(
            sseEmitter -> sseEmitter.completeWithError(new RuntimeException("sse ping failed")));
  }

  private boolean ping(SseEmitter sseEmitter) {
    try {
      sseEmitter.send(SseEmitter.event()
          .name("ping")
          .build());
      return true;
    } catch (IOException e) {
      log.error("핑 이벤트 실패", e);
      return false;
    }
  }

}
