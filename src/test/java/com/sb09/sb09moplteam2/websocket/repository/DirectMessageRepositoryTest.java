package com.sb09.sb09moplteam2.websocket.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.sb09moplteam2.config.MockSearchTestConfig;
import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.config.TestJpaConfig;
import com.sb09.sb09moplteam2.websocket.entity.Conversation;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import({QuerydslConfig.class, TestJpaConfig.class, MockSearchTestConfig.class})
class DirectMessageRepositoryTest {

  @Autowired
  private DirectMessageRepository directMessageRepository;

  @Autowired
  private TestEntityManager em;

  private Conversation persistConversation() {
    Conversation conversation = Conversation.createDirect();
    em.persist(conversation);
    return conversation;
  }

  private DirectMessage persistMessage(Conversation conversation, UUID senderId, String content) {
    DirectMessage dm = DirectMessage.of(conversation, senderId, content);
    em.persist(dm);
    return dm;
  }

  // ───────────────────────────── findByConversationOrderBySentAtDesc ─────────────────────────────

  @Test
  @DisplayName("대화방의 메시지를 sentAt 내림차순으로 조회한다")
  void findByConversationOrderBySentAtDesc_내림차순으로_조회한다() throws InterruptedException {
    Conversation conversation = persistConversation();
    UUID senderId = UUID.randomUUID();

    DirectMessage first = persistMessage(conversation, senderId, "첫번째");
    Thread.sleep(5); // sentAt 값이 명확히 갈리도록
    DirectMessage second = persistMessage(conversation, senderId, "두번째");
    em.flush();

    List<DirectMessage> result = directMessageRepository.findByConversationOrderBySentAtDesc(
        conversation, PageRequest.of(0, 10));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(second.getId());
    assertThat(result.get(1).getId()).isEqualTo(first.getId());
  }

  @Test
  @DisplayName("다른 대화방의 메시지는 조회되지 않는다")
  void findByConversationOrderBySentAtDesc_다른_대화방은_제외된다() {
    Conversation conversation1 = persistConversation();
    Conversation conversation2 = persistConversation();
    UUID senderId = UUID.randomUUID();

    persistMessage(conversation1, senderId, "대화방1 메시지");
    persistMessage(conversation2, senderId, "대화방2 메시지");
    em.flush();

    List<DirectMessage> result = directMessageRepository.findByConversationOrderBySentAtDesc(
        conversation1, PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContent()).isEqualTo("대화방1 메시지");
  }

  // ───────────────────────────── findByConversationWithCursor ─────────────────────────────

  @Test
  @DisplayName("커서 이전(sentAt이 더 작은) 메시지만 조회한다")
  void findByConversationWithCursor_커서_이전_메시지만_조회한다() throws InterruptedException {
    Conversation conversation = persistConversation();
    UUID senderId = UUID.randomUUID();

    DirectMessage older = persistMessage(conversation, senderId, "오래된 메시지");
    Thread.sleep(5);
    DirectMessage cursorTarget = persistMessage(conversation, senderId, "커서 기준 메시지");
    Thread.sleep(5);
    persistMessage(conversation, senderId, "최신 메시지"); // 커서보다 최신 - 제외되어야 함
    em.flush();

    List<DirectMessage> result = directMessageRepository.findByConversationWithCursor(
        conversation, cursorTarget.getSentAt(), cursorTarget.getId(), PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(older.getId());
  }

  @Test
  @DisplayName("sentAt이 같으면 자기 자신을 커서로 조회했을 때 자기 자신은 결과에 포함되지 않는다")
  void findByConversationWithCursor_sentAt_동일시_자기자신은_제외된다() {
    Conversation conversation = persistConversation();
    UUID senderId = UUID.randomUUID();
    Instant sameTime = Instant.now();

    DirectMessage dm1 = DirectMessage.of(conversation, senderId, "메시지1");
    DirectMessage dm2 = DirectMessage.of(conversation, senderId, "메시지2");
    org.springframework.test.util.ReflectionTestUtils.setField(dm1, "sentAt", sameTime);
    org.springframework.test.util.ReflectionTestUtils.setField(dm2, "sentAt", sameTime);
    em.persist(dm1);
    em.persist(dm2);
    em.flush();
    em.clear(); // 영속성 컨텍스트 비우기 - DB에 실제 저장된 값을 가져오도록

    DirectMessage dm1Reloaded = em.find(DirectMessage.class, dm1.getId());
    DirectMessage dm2Reloaded = em.find(DirectMessage.class, dm2.getId());
    Instant persistedSentAt = dm1Reloaded.getSentAt(); // DB에 실제 저장된(반올림된) 값

    List<DirectMessage> resultWithDm1AsCursor = directMessageRepository.findByConversationWithCursor(
        conversation, persistedSentAt, dm1Reloaded.getId(), PageRequest.of(0, 10));
    List<DirectMessage> resultWithDm2AsCursor = directMessageRepository.findByConversationWithCursor(
        conversation, persistedSentAt, dm2Reloaded.getId(), PageRequest.of(0, 10));

    assertThat(resultWithDm1AsCursor).extracting(DirectMessage::getId).doesNotContain(dm1.getId());
    assertThat(resultWithDm2AsCursor).extracting(DirectMessage::getId).doesNotContain(dm2.getId());

    int matchedCount = resultWithDm1AsCursor.size() + resultWithDm2AsCursor.size();
    assertThat(matchedCount).isEqualTo(1);
  }

  // ───────────────────────────── findTopByConversationIdOrderBySentAtDesc ─────────────────────────────

  @Test
  @DisplayName("가장 최근 메시지 하나를 조회한다")
  void findTopByConversationIdOrderBySentAtDesc_최근_메시지를_조회한다() throws InterruptedException {
    Conversation conversation = persistConversation();
    UUID senderId = UUID.randomUUID();

    persistMessage(conversation, senderId, "오래된 메시지");
    Thread.sleep(5);
    DirectMessage latest = persistMessage(conversation, senderId, "최신 메시지");
    em.flush();

    Optional<DirectMessage> result = directMessageRepository
        .findTopByConversationIdOrderBySentAtDesc(conversation.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(latest.getId());
  }

  @Test
  @DisplayName("메시지가 없으면 비어있다")
  void findTopByConversationIdOrderBySentAtDesc_메시지가_없으면_비어있다() {
    Conversation conversation = persistConversation();

    Optional<DirectMessage> result = directMessageRepository
        .findTopByConversationIdOrderBySentAtDesc(conversation.getId());

    assertThat(result).isEmpty();
  }

  // ───────────────────────────── existsByConversationId ─────────────────────────────

  @Test
  @DisplayName("메시지가 하나라도 있으면 true를 반환한다")
  void existsByConversationId_메시지가_있으면_true() {
    Conversation conversation = persistConversation();
    persistMessage(conversation, UUID.randomUUID(), "메시지");
    em.flush();

    boolean result = directMessageRepository.existsByConversationId(conversation.getId());

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("메시지가 없으면 false를 반환한다")
  void existsByConversationId_메시지가_없으면_false() {
    Conversation conversation = persistConversation();

    boolean result = directMessageRepository.existsByConversationId(conversation.getId());

    assertThat(result).isFalse();
  }

  // ───────────────────────────── existsByConversationIdAndSenderIdNotAndSentAtAfter ─────────────────────────────
  // (읽지 않은 메시지 존재 여부 판단에 사용되는 쿼리 - ConversationMapper.calculateHasUnread에서 사용)

  @Test
  @DisplayName("상대방이 보낸, lastReadAt 이후 메시지가 있으면 true를 반환한다")
  void existsByConversationIdAndSenderIdNotAndSentAtAfter_안읽은_메시지가_있으면_true() {
    Conversation conversation = persistConversation();
    UUID myUserId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    Instant lastReadAt = Instant.now();

    persistMessage(conversation, otherUserId, "안읽은 메시지");
    em.flush();

    boolean result = directMessageRepository.existsByConversationIdAndSenderIdNotAndSentAtAfter(
        conversation.getId(), myUserId, lastReadAt.minusSeconds(10));

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("내가 보낸 메시지만 있으면 false를 반환한다 (자기 메시지는 안읽음 대상 아님)")
  void existsByConversationIdAndSenderIdNotAndSentAtAfter_내가_보낸_메시지만_있으면_false() {
    Conversation conversation = persistConversation();
    UUID myUserId = UUID.randomUUID();
    Instant lastReadAt = Instant.now().minusSeconds(60);

    persistMessage(conversation, myUserId, "내가 보낸 메시지");
    em.flush();

    boolean result = directMessageRepository.existsByConversationIdAndSenderIdNotAndSentAtAfter(
        conversation.getId(), myUserId, lastReadAt);

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("lastReadAt 이전 메시지만 있으면 false를 반환한다")
  void existsByConversationIdAndSenderIdNotAndSentAtAfter_읽은_이후_메시지가_없으면_false() {
    Conversation conversation = persistConversation();
    UUID myUserId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();

    DirectMessage oldMessage = persistMessage(conversation, otherUserId, "이미 읽은 메시지");
    em.flush();
    Instant lastReadAt = oldMessage.getSentAt().plusSeconds(60); // 메시지보다 미래 시각

    boolean result = directMessageRepository.existsByConversationIdAndSenderIdNotAndSentAtAfter(
        conversation.getId(), myUserId, lastReadAt);

    assertThat(result).isFalse();
  }
}
