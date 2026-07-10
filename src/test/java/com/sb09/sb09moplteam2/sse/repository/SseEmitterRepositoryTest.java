package com.sb09.sb09moplteam2.sse.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sb09.sb09moplteam2.sse.SseEmitterRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRepositoryTest {

  private SseEmitterRepository sseEmitterRepository;

  @BeforeEach
  void setUp() {
    sseEmitterRepository = new SseEmitterRepository();
  }

  @Test
  void save_새로운_receiverId_저장() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter = mock(SseEmitter.class);

    SseEmitter result = sseEmitterRepository.save(receiverId, emitter);

    assertThat(result).isEqualTo(emitter);
    assertThat(sseEmitterRepository.findAll()).containsExactly(emitter);
  }

  @Test
  void save_기존_receiverId에_emitter_추가() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);

    sseEmitterRepository.save(receiverId, emitter1);
    sseEmitterRepository.save(receiverId, emitter2);

    List<SseEmitter> result = sseEmitterRepository.findAllByReceiverIdsIn(Set.of(receiverId));
    assertThat(result).containsExactly(emitter1, emitter2);
  }

  @Test
  void findAllByReceiverIdsIn_여러_receiverId에_해당하는_emitter_모두_반환() {
    UUID receiverId1 = UUID.randomUUID();
    UUID receiverId2 = UUID.randomUUID();
    UUID receiverId3 = UUID.randomUUID();

    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);
    SseEmitter emitter3 = mock(SseEmitter.class);

    sseEmitterRepository.save(receiverId1, emitter1);
    sseEmitterRepository.save(receiverId2, emitter2);
    sseEmitterRepository.save(receiverId3, emitter3);

    List<SseEmitter> result = sseEmitterRepository.findAllByReceiverIdsIn(
        Set.of(receiverId1, receiverId2));

    assertThat(result).containsExactlyInAnyOrder(emitter1, emitter2);
    assertThat(result).doesNotContain(emitter3);
  }

  @Test
  void findAllByReceiverIdsIn_해당하는_receiverId_없으면_빈_리스트_반환() {
    UUID receiverId = UUID.randomUUID();
    sseEmitterRepository.save(receiverId, mock(SseEmitter.class));

    List<SseEmitter> result = sseEmitterRepository.findAllByReceiverIdsIn(
        Set.of(UUID.randomUUID()));

    assertThat(result).isEmpty();
  }

  @Test
  void findAllByReceiverIdsIn_조회대상_receiverIds가_비어있으면_빈_리스트_반환() {
    sseEmitterRepository.save(UUID.randomUUID(), mock(SseEmitter.class));

    List<SseEmitter> result = sseEmitterRepository.findAllByReceiverIdsIn(Set.of());

    assertThat(result).isEmpty();
  }

  @Test
  void findAll_저장된_모든_emitter_반환() {
    UUID receiverId1 = UUID.randomUUID();
    UUID receiverId2 = UUID.randomUUID();

    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);
    SseEmitter emitter3 = mock(SseEmitter.class);

    sseEmitterRepository.save(receiverId1, emitter1);
    sseEmitterRepository.save(receiverId1, emitter2);
    sseEmitterRepository.save(receiverId2, emitter3);

    List<SseEmitter> result = sseEmitterRepository.findAll();

    assertThat(result).containsExactlyInAnyOrder(emitter1, emitter2, emitter3);
  }

  @Test
  void findAll_저장된_것_없으면_빈_리스트_반환() {
    List<SseEmitter> result = sseEmitterRepository.findAll();

    assertThat(result).isEmpty();
  }

  @Test
  void delete_emitter_하나만_있을때_삭제하면_키_자체가_사라짐() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter = mock(SseEmitter.class);
    sseEmitterRepository.save(receiverId, emitter);

    sseEmitterRepository.delete(receiverId, emitter);

    assertThat(sseEmitterRepository.findAll()).isEmpty();
    assertThat(sseEmitterRepository.findAllByReceiverIdsIn(Set.of(receiverId))).isEmpty();
  }

  @Test
  void delete_emitter_여러개중_하나만_삭제하면_나머지는_유지() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);
    sseEmitterRepository.save(receiverId, emitter1);
    sseEmitterRepository.save(receiverId, emitter2);

    sseEmitterRepository.delete(receiverId, emitter1);

    List<SseEmitter> remaining = sseEmitterRepository.findAllByReceiverIdsIn(Set.of(receiverId));
    assertThat(remaining).containsExactly(emitter2);
  }

  @Test
  void delete_존재하지_않는_receiverId면_아무_동작_안함() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter = mock(SseEmitter.class);

    sseEmitterRepository.delete(receiverId, emitter);

    assertThat(sseEmitterRepository.findAll()).isEmpty();
  }

  @Test
  void delete_존재하지_않는_emitter면_기존_리스트_유지() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter notSavedEmitter = mock(SseEmitter.class);
    sseEmitterRepository.save(receiverId, emitter1);

    sseEmitterRepository.delete(receiverId, notSavedEmitter);

    List<SseEmitter> remaining = sseEmitterRepository.findAllByReceiverIdsIn(Set.of(receiverId));
    assertThat(remaining).containsExactly(emitter1);
  }
}