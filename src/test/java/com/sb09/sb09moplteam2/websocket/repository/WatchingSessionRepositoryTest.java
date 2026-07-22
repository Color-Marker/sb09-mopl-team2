package com.sb09.sb09moplteam2.websocket.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.sb09.sb09moplteam2.config.MockSearchTestConfig;
import com.sb09.sb09moplteam2.config.QuerydslConfig;
import com.sb09.sb09moplteam2.config.TestJpaConfig;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
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
class WatchingSessionRepositoryTest {

  @Autowired
  private WatchingSessionRepository watchingSessionRepository;

  @Autowired
  private TestEntityManager em;

  private WatchingSession persistSession(UUID userId, UUID contentId) {
    WatchingSession session = WatchingSession.create(userId, contentId);
    em.persist(session);
    return session;
  }

  private WatchingSession persistSessionWithStartedAt(UUID userId, UUID contentId, Instant startedAt) {
    WatchingSession session = WatchingSession.create(userId, contentId);
    setField(session, "startedAt", startedAt);
    em.persist(session);
    return session;
  }

  // ───────────────────────────── findByUserIdAndStatus ─────────────────────────────

  @Test
  @DisplayName("ACTIVE 상태의 세션이 있으면 조회된다")
  void findByUserIdAndStatus_ACTIVE_세션이_있으면_조회된다() {
    UUID userId = UUID.randomUUID();
    WatchingSession session = persistSession(userId, UUID.randomUUID());
    em.flush();

    Optional<WatchingSession> result =
        watchingSessionRepository.findByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(session.getId());
  }

  @Test
  @DisplayName("세션이 종료(ENDED)되었으면 ACTIVE 조회에 나오지 않는다")
  void findByUserIdAndStatus_종료된_세션은_조회되지_않는다() {
    UUID userId = UUID.randomUUID();
    WatchingSession session = persistSession(userId, UUID.randomUUID());
    session.end();
    em.flush();

    Optional<WatchingSession> result =
        watchingSessionRepository.findByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("해당 유저의 세션이 없으면 비어있다")
  void findByUserIdAndStatus_세션이_없으면_비어있다() {
    UUID userId = UUID.randomUUID();

    Optional<WatchingSession> result =
        watchingSessionRepository.findByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE);

    assertThat(result).isEmpty();
  }

  // ───────────────────────────── findByUserId ─────────────────────────────

  @Test
  @DisplayName("특정 유저의 세션 전체를 조회한다 (ACTIVE, ENDED 무관)")
  void findByUserId_전체_세션을_조회한다() {
    UUID userId = UUID.randomUUID();
    WatchingSession active = persistSession(userId, UUID.randomUUID());
    WatchingSession ended = persistSession(userId, UUID.randomUUID());
    ended.end();
    em.flush();

    List<WatchingSession> result = watchingSessionRepository.findByUserId(userId);

    assertThat(result).hasSize(2);
  }

  // ───────────────────────────── findByContentId ─────────────────────────────

  @Test
  @DisplayName("콘텐츠의 시청 세션을 startedAt 내림차순으로 조회한다")
  void findByContentId_startedAt_내림차순으로_조회한다() {
    UUID contentId = UUID.randomUUID();
    Instant now = Instant.now();

    WatchingSession older = persistSessionWithStartedAt(UUID.randomUUID(), contentId, now.minusSeconds(60));
    WatchingSession newer = persistSessionWithStartedAt(UUID.randomUUID(), contentId, now);
    em.flush();

    List<WatchingSession> result = watchingSessionRepository.findByContentId(
        contentId, PageRequest.of(0, 10));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(newer.getId());
    assertThat(result.get(1).getId()).isEqualTo(older.getId());
  }

  @Test
  @DisplayName("다른 콘텐츠의 세션은 조회되지 않는다")
  void findByContentId_다른_콘텐츠는_제외된다() {
    UUID contentId = UUID.randomUUID();
    UUID otherContentId = UUID.randomUUID();

    persistSession(UUID.randomUUID(), contentId);
    persistSession(UUID.randomUUID(), otherContentId);
    em.flush();

    List<WatchingSession> result = watchingSessionRepository.findByContentId(
        contentId, PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
  }

  // ───────────────────────────── findByContentIdWithCursor ─────────────────────────────

  @Test
  @DisplayName("커서 이전(startedAt이 더 작은) 세션만 조회한다")
  void findByContentIdWithCursor_커서_이전_세션만_조회한다() {
    UUID contentId = UUID.randomUUID();
    Instant now = Instant.now();

    WatchingSession older = persistSessionWithStartedAt(UUID.randomUUID(), contentId, now.minusSeconds(120));
    WatchingSession cursorTarget = persistSessionWithStartedAt(UUID.randomUUID(), contentId, now.minusSeconds(60));
    persistSessionWithStartedAt(UUID.randomUUID(), contentId, now); // 커서보다 최신 - 제외되어야 함
    em.flush();
    em.clear(); // 영속성 컨텍스트 비우기 - DB에 실제 저장된 값을 가져오도록

    WatchingSession cursorTargetReloaded = em.find(WatchingSession.class, cursorTarget.getId());

    List<WatchingSession> result = watchingSessionRepository.findByContentIdWithCursor(
        contentId, cursorTargetReloaded.getStartedAt(), cursorTargetReloaded.getId(), PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(older.getId());
  }

  @Test
  @DisplayName("startedAt이 같으면 자기 자신을 커서로 조회했을 때 자기 자신은 결과에 포함되지 않는다")
  void findByContentIdWithCursor_startedAt_동일시_자기자신은_제외된다() {
    UUID contentId = UUID.randomUUID();
    Instant sameTime = Instant.now();

    WatchingSession s1 = persistSessionWithStartedAt(UUID.randomUUID(), contentId, sameTime);
    WatchingSession s2 = persistSessionWithStartedAt(UUID.randomUUID(), contentId, sameTime);
    em.flush();
    em.clear(); // 영속성 컨텍스트 비우기 - 이후 조회 시 DB에 실제 저장된 값을 가져오도록

    WatchingSession s1Reloaded = em.find(WatchingSession.class, s1.getId());
    WatchingSession s2Reloaded = em.find(WatchingSession.class, s2.getId());
    Instant persistedStartedAt = s1Reloaded.getStartedAt(); // DB에 실제 저장된(반올림된) 값

    List<WatchingSession> resultWithS1AsCursor = watchingSessionRepository.findByContentIdWithCursor(
        contentId, persistedStartedAt, s1Reloaded.getId(), PageRequest.of(0, 10));
    List<WatchingSession> resultWithS2AsCursor = watchingSessionRepository.findByContentIdWithCursor(
        contentId, persistedStartedAt, s2Reloaded.getId(), PageRequest.of(0, 10));

    assertThat(resultWithS1AsCursor).extracting(WatchingSession::getId).doesNotContain(s1.getId());
    assertThat(resultWithS2AsCursor).extracting(WatchingSession::getId).doesNotContain(s2.getId());

    int matchedCount = resultWithS1AsCursor.size() + resultWithS2AsCursor.size();
    assertThat(matchedCount).isEqualTo(1);
  }

  // ───────────────────────────── existsByUserIdAndStatus ─────────────────────────────

  @Test
  @DisplayName("ACTIVE 세션이 있으면 true를 반환한다")
  void existsByUserIdAndStatus_ACTIVE_세션이_있으면_true() {
    UUID userId = UUID.randomUUID();
    persistSession(userId, UUID.randomUUID());
    em.flush();

    boolean result = watchingSessionRepository
        .existsByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("세션이 종료되었으면 ACTIVE 존재 여부는 false를 반환한다")
  void existsByUserIdAndStatus_종료되었으면_false() {
    UUID userId = UUID.randomUUID();
    WatchingSession session = persistSession(userId, UUID.randomUUID());
    session.end();
    em.flush();

    boolean result = watchingSessionRepository
        .existsByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE);

    assertThat(result).isFalse();
  }

  // ───────────────────────────── endAllActiveStartedBefore ─────────────────────────────

  @Test
  @DisplayName("기준 시각 이전에 시작된 ACTIVE 세션만 일괄 종료한다")
  void endAllActiveStartedBefore_오래된_ACTIVE_세션만_종료한다() {
    Instant now = Instant.now();
    // 7시간 전 시작된 유령 ACTIVE 세션 → 종료 대상
    WatchingSession stale = persistSessionWithStartedAt(
        UUID.randomUUID(), UUID.randomUUID(), now.minusSeconds(7 * 3600));
    // 방금 시작된 정상 ACTIVE 세션 → 유지
    WatchingSession recent = persistSession(UUID.randomUUID(), UUID.randomUUID());
    // 오래됐지만 이미 ENDED인 세션 → 대상 아님
    WatchingSession alreadyEnded = persistSessionWithStartedAt(
        UUID.randomUUID(), UUID.randomUUID(), now.minusSeconds(8 * 3600));
    alreadyEnded.end();
    em.flush();
    em.clear();

    int ended = watchingSessionRepository.endAllActiveStartedBefore(
        now.minusSeconds(6 * 3600), now);

    assertThat(ended).isEqualTo(1);
    assertThat(watchingSessionRepository.findById(stale.getId()).orElseThrow().getStatus())
        .isEqualTo(WatchingSessionStatus.ENDED);
    assertThat(watchingSessionRepository.findById(recent.getId()).orElseThrow().getStatus())
        .isEqualTo(WatchingSessionStatus.ACTIVE);
  }

  @Test
  @DisplayName("종료 대상이 없으면 0을 반환한다")
  void endAllActiveStartedBefore_대상없으면_0을_반환한다() {
    persistSession(UUID.randomUUID(), UUID.randomUUID());
    em.flush();
    em.clear();

    int ended = watchingSessionRepository.endAllActiveStartedBefore(
        Instant.now().minusSeconds(6 * 3600), Instant.now());

    assertThat(ended).isZero();
  }
}
