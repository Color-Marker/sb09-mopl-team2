package com.sb09.sb09moplteam2.websocket.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.config.TestJpaConfig;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import({QuerydslConfig.class, TestJpaConfig.class})
class ConversationRepositoryTest {

  @Autowired
  private ConversationRepository conversationRepository;

  @Autowired
  private TestEntityManager em;

  private Conversation createConversationWithParticipants(UUID userA, UUID userB, Instant lastMessageAt) {
    Conversation conversation = Conversation.createDirect();
    conversation.updateLastMessageAt(lastMessageAt);
    em.persist(conversation);
    em.persist(ConversationParticipant.of(conversation, userA));
    em.persist(ConversationParticipant.of(conversation, userB));
    return conversation;
  }

  // ───────────────────────────── findAllByParticipantUserId ─────────────────────────────

  @Test
  @DisplayName("참여 중인 대화방을 lastMessageAt 내림차순으로 조회한다")
  void findAllByParticipantUserId_lastMessageAt_내림차순으로_조회한다() {
    UUID myUserId = UUID.randomUUID();
    Instant now = Instant.now();

    createConversationWithParticipants(myUserId, UUID.randomUUID(), now.minusSeconds(60));
    createConversationWithParticipants(myUserId, UUID.randomUUID(), now);
    em.flush();

    List<Conversation> result = conversationRepository.findAllByParticipantUserId(
        myUserId, PageRequest.of(0, 10));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getLastMessageAt()).isAfterOrEqualTo(result.get(1).getLastMessageAt());
  }

  @Test
  @DisplayName("참여하지 않은 대화방은 조회되지 않는다")
  void findAllByParticipantUserId_참여하지_않은_대화방은_제외된다() {
    UUID myUserId = UUID.randomUUID();
    createConversationWithParticipants(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
    em.flush();

    List<Conversation> result = conversationRepository.findAllByParticipantUserId(
        myUserId, PageRequest.of(0, 10));

    assertThat(result).isEmpty();
  }

  // ───────────────────────────── findAllByParticipantUserIdWithCursor ─────────────────────────────

  @Test
  @DisplayName("커서 이전(lastMessageAt이 더 작은) 대화방만 조회한다")
  void findAllByParticipantUserIdWithCursor_커서_이전_대화방만_조회한다() {
    UUID myUserId = UUID.randomUUID();
    Instant now = Instant.now();

    Conversation older = createConversationWithParticipants(myUserId, UUID.randomUUID(), now.minusSeconds(120));
    Conversation cursorTarget = createConversationWithParticipants(myUserId, UUID.randomUUID(), now.minusSeconds(60));
    createConversationWithParticipants(myUserId, UUID.randomUUID(), now); // 커서보다 최신 - 제외되어야 함
    em.flush();
    em.clear(); // 영속성 컨텍스트 비우기 - DB에 실제 저장된 값을 가져오도록

    Conversation cursorTargetReloaded = em.find(Conversation.class, cursorTarget.getId());

    List<Conversation> result = conversationRepository.findAllByParticipantUserIdWithCursor(
        myUserId, cursorTargetReloaded.getLastMessageAt(), cursorTargetReloaded.getId(), PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(older.getId());
  }

  @Test
  @DisplayName("lastMessageAt이 같으면 id로 이차 정렬하여 커서 이후를 구분한다")
  void findAllByParticipantUserIdWithCursor_lastMessageAt_동일시_id로_구분한다() {
    UUID myUserId = UUID.randomUUID();
    Instant sameTime = Instant.now();

    Conversation c1 = createConversationWithParticipants(myUserId, UUID.randomUUID(), sameTime);
    Conversation c2 = createConversationWithParticipants(myUserId, UUID.randomUUID(), sameTime);
    em.flush();

    // c1을 커서로 조회했을 때와 c2를 커서로 조회했을 때를 각각 확인
    List<Conversation> resultWithC1AsCursor = conversationRepository.findAllByParticipantUserIdWithCursor(
        myUserId, sameTime, c1.getId(), PageRequest.of(0, 10));
    List<Conversation> resultWithC2AsCursor = conversationRepository.findAllByParticipantUserIdWithCursor(
        myUserId, sameTime, c2.getId(), PageRequest.of(0, 10));

    // 둘 중 정확히 하나는 "나머지 하나만" 반환해야 하고, 자기 자신은 포함하면 안 됨
    assertThat(resultWithC1AsCursor).doesNotContain(c1);
    assertThat(resultWithC2AsCursor).doesNotContain(c2);

    // 둘의 합집합 크기가 정확히 1이어야 함 (DB가 실제로 정렬한 순서상 "더 작은" 쪽 하나만 나옴)
    int matchedCount = resultWithC1AsCursor.size() + resultWithC2AsCursor.size();
    assertThat(matchedCount).isEqualTo(1);
  }

  // ───────────────────────────── findAllByParticipantUserIdNoPaging ─────────────────────────────

  @Test
  @DisplayName("페이징 없이 참여 중인 대화방 전체를 조회한다")
  void findAllByParticipantUserIdNoPaging_전체_조회한다() {
    UUID myUserId = UUID.randomUUID();
    for (int i = 0; i < 5; i++) {
      createConversationWithParticipants(myUserId, UUID.randomUUID(), Instant.now().minusSeconds(i));
    }
    em.flush();

    List<Conversation> result = conversationRepository.findAllByParticipantUserIdNoPaging(myUserId);

    assertThat(result).hasSize(5);
  }

  // ───────────────────────────── Pageable limit 동작 확인 ─────────────────────────────

  @Test
  @DisplayName("limit+1 조회 시 hasNext 판단용으로 정확히 그 개수만큼 반환된다")
  void findAllByParticipantUserId_limit_plus_1개를_반환한다() {
    UUID myUserId = UUID.randomUUID();
    for (int i = 0; i < 5; i++) {
      createConversationWithParticipants(myUserId, UUID.randomUUID(), Instant.now().minusSeconds(i));
    }
    em.flush();

    Pageable limitPlusOne = PageRequest.of(0, 3 + 1);
    List<Conversation> result = conversationRepository.findAllByParticipantUserId(myUserId, limitPlusOne);

    assertThat(result).hasSize(4);
  }
}
