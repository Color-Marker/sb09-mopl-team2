package com.sb09.sb09moplteam2.sse.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sb09.sb09moplteam2.config.RedisConfig;
import com.sb09.sb09moplteam2.sse.SseMessage;
import com.sb09.sb09moplteam2.sse.SseEmitterRepository;
import com.sb09.sb09moplteam2.sse.SseMessageRepository;
import com.sb09.sb09moplteam2.sse.SseService;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

  @Mock
  private SseEmitterRepository sseEmitterRepository;

  @Mock
  private SseMessageRepository sseMessageRepository;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  private SseService sseService;

  private static final long TIMEOUT = 30_000L;

  @BeforeEach
  void setUp() {
    sseService = new SseService(sseEmitterRepository, sseMessageRepository, redisTemplate);
    ReflectionTestUtils.setField(sseService, "timeout", TIMEOUT);
  }

  @Test
  void connect_lastEventId_없음_핑_전송_저장소_저장() {
    UUID receiverId = UUID.randomUUID();

    try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
      SseEmitter result = sseService.connect(receiverId, null);

      SseEmitter createdEmitter = mocked.constructed().get(0);
      assertThat(mocked.constructed()).hasSize(1);
      assertThat(result).isEqualTo(createdEmitter);

      verify(createdEmitter).onCompletion(any());
      verify(createdEmitter).onTimeout(any());
      verify(createdEmitter).onError(any());
      verify(sseEmitterRepository).save(receiverId, createdEmitter);

      verify(createdEmitter, times(1)).send(anySet());
      verifyNoInteractions(sseMessageRepository);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void connect_lastEventId_있음_메시지_재전송_핑_안보냄() {
    UUID receiverId = UUID.randomUUID();
    UUID lastEventId = UUID.randomUUID();

    SseMessage missedMessage1 = mock(SseMessage.class);
    SseMessage missedMessage2 = mock(SseMessage.class);
    given(missedMessage1.toEvent()).willReturn(Set.of());
    given(missedMessage2.toEvent()).willReturn(Set.of());

    given(sseMessageRepository.findAllByEventIdAfterAndReceiverId(lastEventId, receiverId))
        .willReturn(List.of(missedMessage1, missedMessage2));

    try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
      sseService.connect(receiverId, lastEventId);

      SseEmitter createdEmitter = mocked.constructed().get(0);
      verify(sseEmitterRepository).save(receiverId, createdEmitter);
      verify(sseMessageRepository).findAllByEventIdAfterAndReceiverId(lastEventId, receiverId);

      verify(createdEmitter, times(2)).send(anySet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void send_메시지_저장_redis_발행() throws IOException {
    UUID receiverId1 = UUID.randomUUID();
    UUID receiverId2 = UUID.randomUUID();
    List<UUID> receiverIds = List.of(receiverId1, receiverId2);
    String eventName = "notifications";
    String data = "hello";

    UUID eventId = UUID.randomUUID();
    SseMessage savedMessage = mock(SseMessage.class);
    given(savedMessage.eventId()).willReturn(eventId);
    given(savedMessage.receiverIds()).willReturn(Set.copyOf(receiverIds));
    given(savedMessage.eventName()).willReturn(eventName);
    given(savedMessage.eventData()).willReturn(data);
    given(sseMessageRepository.save(any(SseMessage.class))).willReturn(savedMessage);

    sseService.publishToRedis(receiverIds, eventName, data);

    ArgumentCaptor<SseMessage> messageCaptor = ArgumentCaptor.forClass(SseMessage.class);
    verify(sseMessageRepository).save(messageCaptor.capture());
    SseMessage createdMessage = messageCaptor.getValue();
    assertThat(createdMessage.receiverIds()).containsExactlyInAnyOrder(receiverId1, receiverId2);
    assertThat(createdMessage.eventName()).isEqualTo(eventName);
    assertThat(createdMessage.eventData()).isEqualTo(data);

    ArgumentCaptor<SseMessage> payloadCaptor = ArgumentCaptor.forClass(SseMessage.class);
    verify(redisTemplate).convertAndSend(eq(RedisConfig.SSE_CHANNEL), payloadCaptor.capture());
    SseMessage payload = payloadCaptor.getValue();
    assertThat(payload.eventId()).isEqualTo(eventId);
    assertThat(payload.receiverIds()).containsExactlyInAnyOrder(receiverId1, receiverId2);
    assertThat(payload.eventName()).isEqualTo(eventName);
    assertThat(payload.eventData()).isEqualTo(data);
  }

  @Test
  void send_redis_페이로드_수신자_emitter_전송() throws IOException {
    UUID eventId = UUID.randomUUID();
    UUID receiverId1 = UUID.randomUUID();
    UUID receiverId2 = UUID.randomUUID();
    Set<UUID> receiverIds = Set.of(receiverId1, receiverId2);
    String eventName = "notifications";
    String data = "hello";

    SseMessage payload = new SseMessage(eventId, receiverIds, eventName, data);

    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);
    given(sseEmitterRepository.findAllByReceiverIdsIn(receiverIds))
        .willReturn(List.of(emitter1, emitter2));

    sseService.send(payload);

    ArgumentCaptor<Set<DataWithMediaType>> eventCaptor = ArgumentCaptor.forClass(Set.class);
    verify(emitter1).send(eventCaptor.capture());
    verify(emitter2).send(eventCaptor.capture());

    List<Set<DataWithMediaType>> captured = eventCaptor.getAllValues();
    assertThat(captured.get(0)).isEqualTo(captured.get(1));

    verifyNoInteractions(sseMessageRepository);
  }

  @Test
  void cleanUp_핑_실패_emitter_completeWithError_호출() throws IOException {
    SseEmitter healthyEmitter = mock(SseEmitter.class);
    SseEmitter deadEmitter = mock(SseEmitter.class);
    doThrow(new IOException("연결 끊김")).when(deadEmitter).send(anySet());

    given(sseEmitterRepository.findAll()).willReturn(List.of(healthyEmitter, deadEmitter));

    sseService.cleanUp();

    verify(healthyEmitter, never()).completeWithError(any());
    verify(deadEmitter, times(1)).completeWithError(any(RuntimeException.class));
  }
}