package com.sb09.sb09moplteam2.websocket.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.config.MockSearchTestConfig;
import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.config.TestJpaConfig;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.ConversationParticipant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class, TestJpaConfig.class, MockSearchTestConfig.class})
class ConversationParticipantRepositoryTest {

  @Autowired
  private ConversationParticipantRepository conversationParticipantRepository;

  @Autowired
  private TestEntityManager em;

  private Conversation persistDirectConversation(UUID userA, UUID userB) {
    Conversation conversation = Conversation.createDirect();
    em.persist(conversation);
    em.persist(ConversationParticipant.of(conversation, userA));
    em.persist(ConversationParticipant.of(conversation, userB));
    return conversation;
  }

  // ───────────────────────────── findExistingDirectConversation ─────────────────────────────

  @Test
  @DisplayName("두 유저 사이에 DIRECT 대화방이 있으면 조회된다")
  void findExistingDirectConversation_존재하면_조회된다() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    Conversation conversation = persistDirectConversation(userA, userB);
    em.flush();

    Optional<Conversation> result =
        conversationParticipantRepository.findExistingDirectConversation(userA, userB);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(conversation.getId());
  }

  @Test
  @DisplayName("유저 순서를 바꿔서 조회해도 동일한 대화방을 찾는다")
  void findExistingDirectConversation_순서를_바꿔도_동일하게_조회된다() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    Conversation conversation = persistDirectConversation(userA, userB);
    em.flush();

    Optional<Conversation> result =
        conversationParticipantRepository.findExistingDirectConversation(userB, userA);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(conversation.getId());
  }

  @Test
  @DisplayName("대화방이 없으면 조회되지 않는다")
  void findExistingDirectConversation_없으면_비어있다() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();

    Optional<Conversation> result =
        conversationParticipantRepository.findExistingDirectConversation(userA, userB);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("한쪽만 참여 중이고 상대는 다른 사람이면 조회되지 않는다")
  void findExistingDirectConversation_상대가_다르면_조회되지_않는다() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    UUID userC = UUID.randomUUID();
    persistDirectConversation(userA, userC); // A-C 대화방만 존재
    em.flush();

    Optional<Conversation> result =
        conversationParticipantRepository.findExistingDirectConversation(userA, userB);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("GROUP 타입 대화방에 두 유저가 함께 있어도 조회되지 않는다")
  void findExistingDirectConversation_GROUP_타입이면_조회되지_않는다() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    Conversation groupConversation = Conversation.createGroup("스터디방");
    em.persist(groupConversation);
    em.persist(ConversationParticipant.of(groupConversation, userA));
    em.persist(ConversationParticipant.of(groupConversation, userB));
    em.flush();

    Optional<Conversation> result =
        conversationParticipantRepository.findExistingDirectConversation(userA, userB);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("제3자가 포함된 대화방은 정확한 2인 매칭에서 제외된다")
  void findExistingDirectConversation_제3자가_포함되면_조회되지_않는다() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    UUID userC = UUID.randomUUID();
    // GROUP 타입으로 3명이 참여 (DIRECT는 원래 2명이 원칙이라 이 케이스는 방어적 검증)
    Conversation groupConversation = Conversation.createGroup("셋이서");
    em.persist(groupConversation);
    em.persist(ConversationParticipant.of(groupConversation, userA));
    em.persist(ConversationParticipant.of(groupConversation, userB));
    em.persist(ConversationParticipant.of(groupConversation, userC));
    em.flush();

    Optional<Conversation> result =
        conversationParticipantRepository.findExistingDirectConversation(userA, userB);

    assertThat(result).isEmpty();
  }

  // ───────────────────────────── existsByConversationAndUserId ─────────────────────────────

  @Test
  @DisplayName("참여 중이면 true를 반환한다")
  void existsByConversationAndUserId_참여중이면_true() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    Conversation conversation = persistDirectConversation(userA, userB);
    em.flush();

    boolean result = conversationParticipantRepository
        .existsByConversationAndUserId(conversation, userA);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("참여 중이 아니면 false를 반환한다")
  void existsByConversationAndUserId_참여중이_아니면_false() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    UUID strangerId = UUID.randomUUID();
    Conversation conversation = persistDirectConversation(userA, userB);
    em.flush();

    boolean result = conversationParticipantRepository
        .existsByConversationAndUserId(conversation, strangerId);

    assertThat(result).isFalse();
  }

  // ───────────────────────────── findByConversationIdIn (N+1 방지용) ─────────────────────────────

  @Test
  @DisplayName("여러 대화방 ID로 참여자를 한 번에 조회한다")
  void findByConversationIdIn_여러_대화방의_참여자를_한번에_조회한다() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    UUID userC = UUID.randomUUID();
    Conversation conversation1 = persistDirectConversation(userA, userB);
    Conversation conversation2 = persistDirectConversation(userA, userC);
    em.flush();

    List<ConversationParticipant> result = conversationParticipantRepository
        .findByConversationIdIn(List.of(conversation1.getId(), conversation2.getId()));

    assertThat(result).hasSize(4); // 각 대화방당 2명씩
  }
}
