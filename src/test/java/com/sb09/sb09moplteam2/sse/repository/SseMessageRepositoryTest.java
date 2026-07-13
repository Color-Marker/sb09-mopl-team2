package com.sb09.sb09moplteam2.sse.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.sse.SseMessage;
import com.sb09.sb09moplteam2.sse.SseMessageRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SseMessageRepositoryTest {

  private static final String KEY = "sse:message-queue";
  private static final int CAPACITY = 100;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ListOperations<String, Object> listOperations;

  private SseMessageRepository sseMessageRepository;

  @BeforeEach
  void setUp() {
    sseMessageRepository = new SseMessageRepository(redisTemplate);
    ReflectionTestUtils.setField(sseMessageRepository, "eventQueueCapacity", CAPACITY);
  }

  @Test
  void save_메시지_rightPush_후_trim_호출() {
    given(redisTemplate.opsForList()).willReturn(listOperations);
    SseMessage message = mock(SseMessage.class);

    SseMessage result = sseMessageRepository.save(message);

    assertThat(result).isEqualTo(message);
    verify(listOperations).rightPush(KEY, message);
    verify(listOperations).trim(KEY, -CAPACITY, -1);
  }

  @Test
  void findAllByEventIdAfterAndReceiverId_raw가_null이면_빈_리스트_반환() {
    given(redisTemplate.opsForList()).willReturn(listOperations);
    given(listOperations.range(KEY, 0, -1)).willReturn(null);

    List<SseMessage> result = sseMessageRepository.findAllByEventIdAfterAndReceiverId(
        UUID.randomUUID(), UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  @Test
  void findAllByEventIdAfterAndReceiverId_eventId_이후_수신가능한_메시지만_반환() {
    UUID targetEventId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    SseMessage before = mock(SseMessage.class);
    SseMessage target = mock(SseMessage.class); // eventId 일치, skip(1)으로 제외되어야 함
    SseMessage afterReceivable = mock(SseMessage.class);
    SseMessage afterNotReceivable = mock(SseMessage.class);

    given(before.eventId()).willReturn(UUID.randomUUID());
    given(target.eventId()).willReturn(targetEventId);
    given(afterReceivable.isReceivable(receiverId)).willReturn(true);
    given(afterNotReceivable.isReceivable(receiverId)).willReturn(false);

    given(redisTemplate.opsForList()).willReturn(listOperations);
    given(listOperations.range(KEY, 0, -1))
        .willReturn(List.of(before, target, afterReceivable, afterNotReceivable));

    List<SseMessage> result = sseMessageRepository.findAllByEventIdAfterAndReceiverId(
        targetEventId, receiverId);

    assertThat(result).containsExactly(afterReceivable);
  }

  @Test
  void findAllByEventIdAfterAndReceiverId_eventId_못찾으면_빈_리스트_반환() {
    UUID targetEventId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    SseMessage message1 = mock(SseMessage.class);
    SseMessage message2 = mock(SseMessage.class);
    given(message1.eventId()).willReturn(UUID.randomUUID());
    given(message2.eventId()).willReturn(UUID.randomUUID());

    given(redisTemplate.opsForList()).willReturn(listOperations);
    given(listOperations.range(KEY, 0, -1)).willReturn(List.of(message1, message2));

    List<SseMessage> result = sseMessageRepository.findAllByEventIdAfterAndReceiverId(
        targetEventId, receiverId);

    assertThat(result).isEmpty();
  }

  @Test
  void findAllByEventIdAfterAndReceiverId_eventId가_마지막_메시지면_빈_리스트_반환() {
    UUID targetEventId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    SseMessage target = mock(SseMessage.class);
    given(target.eventId()).willReturn(targetEventId);

    given(redisTemplate.opsForList()).willReturn(listOperations);
    given(listOperations.range(KEY, 0, -1)).willReturn(List.of(target));

    List<SseMessage> result = sseMessageRepository.findAllByEventIdAfterAndReceiverId(
        targetEventId, receiverId);

    assertThat(result).isEmpty();
  }

  @Test
  void findAllByEventIdAfterAndReceiverId_수신불가능한_메시지만_있으면_빈_리스트_반환() {
    UUID targetEventId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    SseMessage target = mock(SseMessage.class);
    SseMessage notReceivable = mock(SseMessage.class);

    given(target.eventId()).willReturn(targetEventId);
    given(notReceivable.isReceivable(receiverId)).willReturn(false);

    given(redisTemplate.opsForList()).willReturn(listOperations);
    given(listOperations.range(KEY, 0, -1)).willReturn(List.of(target, notReceivable));

    List<SseMessage> result = sseMessageRepository.findAllByEventIdAfterAndReceiverId(
        targetEventId, receiverId);

    assertThat(result).isEmpty();
  }
}