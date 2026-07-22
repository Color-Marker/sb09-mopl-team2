package com.sb09.sb09moplteam2.websocket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
import com.sb09.sb09moplteam2.websocket.event.WatchingSessionEvent;
import com.sb09.sb09moplteam2.websocket.mapper.WatchingSessionMapper;
import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import com.sb09.sb09moplteam2.websocket.relay.StompBroadcastRelay;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WatchingSessionServiceTest {

  @Mock
  private WatchingSessionRepository watchingSessionRepository;
  @Mock
  private WatchingSessionMapper watchingSessionMapper;
  @Mock
  private StompBroadcastRelay stompBroadcastRelay;

  @InjectMocks
  private WatchingSessionService watchingSessionService;

  private UUID userId;
  private UUID contentId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    contentId = UUID.randomUUID();
  }

  private WatchingSession makeActiveSession(UUID userId, UUID contentId) {
    WatchingSession session = WatchingSession.create(userId, contentId);
    ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
    return session;
  }

  private WatchingSessionDto makeDto(UUID sessionId) {
    return new WatchingSessionDto(
        sessionId,
        Instant.now(),
        new UserSummary(userId, "시청자", null),
        new ContentSummary(contentId, ContentType.movie, "제목", "설명", "thumb.jpg", List.of(), 4.0, 1)
    );
  }

  // ───────────────────────────── findActiveByUserId ─────────────────────────────

  @Test
  void 활성_세션이_있으면_dto를_반환한다() {
    WatchingSession session = makeActiveSession(userId, contentId);
    WatchingSessionDto dto = makeDto(session.getId());

    given(watchingSessionRepository.findByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.of(session));
    given(watchingSessionMapper.toDto(session)).willReturn(dto);

    WatchingSessionDto result = watchingSessionService.findActiveByUserId(userId);

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void 활성_세션이_없으면_null을_반환한다() {
    given(watchingSessionRepository.findByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.empty());

    WatchingSessionDto result = watchingSessionService.findActiveByUserId(userId);

    assertThat(result).isNull();
  }

  // ───────────────────────────── findAllByContentId ─────────────────────────────

  @Test
  void 커서없이_조회하면_findByContentId를_사용한다() {
    WatchingSession session = makeActiveSession(userId, contentId);
    WatchingSessionDto dto = makeDto(session.getId());

    given(watchingSessionRepository.findByContentId(eq(contentId), any(Pageable.class)))
        .willReturn(List.of(session));
    given(watchingSessionMapper.toDto(session)).willReturn(dto);

    CursorResponse<WatchingSessionDto> result = watchingSessionService.findAllByContentId(
        contentId, null, null, 10, "startedAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void 커서가_있으면_findByContentIdWithCursor를_사용한다() {
    Instant cursorTime = Instant.now().minusSeconds(60);
    UUID idAfter = UUID.randomUUID();
    WatchingSession session = makeActiveSession(userId, contentId);
    WatchingSessionDto dto = makeDto(session.getId());

    given(watchingSessionRepository.findByContentIdWithCursor(
        eq(contentId), eq(cursorTime), eq(idAfter), any(Pageable.class)))
        .willReturn(List.of(session));
    given(watchingSessionMapper.toDto(session)).willReturn(dto);

    CursorResponse<WatchingSessionDto> result = watchingSessionService.findAllByContentId(
        contentId, cursorTime.toString(), idAfter, 10, "startedAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
  }

  @Test
  void limit보다_결과가_많으면_hasNext가_true이고_nextCursor가_세팅된다() {
    WatchingSession s1 = makeActiveSession(userId, contentId);
    WatchingSession s2 = makeActiveSession(UUID.randomUUID(), contentId);
    int limit = 1;

    given(watchingSessionRepository.findByContentId(eq(contentId), any(Pageable.class)))
        .willReturn(List.of(s1, s2));
    given(watchingSessionMapper.toDto(s1)).willReturn(makeDto(s1.getId()));

    CursorResponse<WatchingSessionDto> result = watchingSessionService.findAllByContentId(
        contentId, null, null, limit, "startedAt", "DESCENDING");

    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextIdAfter()).isEqualTo(s1.getId());
  }

  // ───────────────────────────── join ─────────────────────────────

  @Test
  void 기존_활성_세션이_없으면_새_세션만_생성하고_JOIN을_브로드캐스트한다() {
    WatchingSessionDto dto = makeDto(UUID.randomUUID());

    given(watchingSessionRepository.findByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.empty());
    given(watchingSessionRepository.save(any(WatchingSession.class)))
        .willAnswer(invocation -> {
          WatchingSession s = invocation.getArgument(0);
          ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
          return s;
        });
    given(watchingSessionMapper.toDto(any(WatchingSession.class))).willReturn(dto);

    UUID resultId = watchingSessionService.join(contentId, userId);

    assertThat(resultId).isNotNull();
    verify(watchingSessionRepository).save(any(WatchingSession.class));

    ArgumentCaptor<WatchingSessionEvent> eventCaptor = ArgumentCaptor.forClass(WatchingSessionEvent.class);
    verify(stompBroadcastRelay, times(1)).broadcast(
        eq("/sub/contents/" + contentId + "/watch"), eventCaptor.capture());
    assertThat(eventCaptor.getValue().type()).isEqualTo("JOIN");
  }

  @Test
  void 기존_활성_세션이_있으면_종료시키고_LEAVE와_JOIN_둘다_브로드캐스트한다() {
    WatchingSession existingSession = makeActiveSession(userId, UUID.randomUUID());
    WatchingSessionDto dto = makeDto(existingSession.getId());

    given(watchingSessionRepository.findByUserIdAndStatus(userId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.of(existingSession));
    given(watchingSessionRepository.save(any(WatchingSession.class)))
        .willAnswer(invocation -> {
          WatchingSession s = invocation.getArgument(0);
          ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
          return s;
        });
    given(watchingSessionMapper.toDto(any(WatchingSession.class))).willReturn(dto);

    watchingSessionService.join(contentId, userId);

    assertThat(existingSession.getStatus()).isEqualTo(WatchingSessionStatus.ENDED);
    verify(watchingSessionRepository).save(any(WatchingSession.class));

    ArgumentCaptor<WatchingSessionEvent> eventCaptor = ArgumentCaptor.forClass(WatchingSessionEvent.class);
    verify(stompBroadcastRelay, times(2)).broadcast(
        org.mockito.ArgumentMatchers.anyString(), eventCaptor.capture());

    List<WatchingSessionEvent> events = eventCaptor.getAllValues();
    assertThat(events).extracting(WatchingSessionEvent::type)
        .containsExactly("LEAVE", "JOIN");
  }

  // ───────────────────────────── leave ─────────────────────────────

  @Test
  void 활성_세션을_leave하면_종료되고_LEAVE를_브로드캐스트한다() {
    WatchingSession session = makeActiveSession(userId, contentId);
    WatchingSessionDto dto = makeDto(session.getId());

    given(watchingSessionRepository.findById(session.getId())).willReturn(Optional.of(session));
    given(watchingSessionMapper.toDto(session)).willReturn(dto);

    watchingSessionService.leave(session.getId());

    assertThat(session.getStatus()).isEqualTo(WatchingSessionStatus.ENDED);
    verify(stompBroadcastRelay).broadcast(
        eq("/sub/contents/" + contentId + "/watch"),
        eq(new WatchingSessionEvent("LEAVE", dto)));
  }

  @Test
  void 이미_종료된_세션을_leave하면_아무것도_하지_않는다() {
    WatchingSession session = makeActiveSession(userId, contentId);
    session.end();

    given(watchingSessionRepository.findById(session.getId())).willReturn(Optional.of(session));

    watchingSessionService.leave(session.getId());

    verify(stompBroadcastRelay, never()).broadcast(any(String.class), any(Object.class));
  }

  @Test
  void 존재하지_않는_세션을_leave하면_아무것도_하지_않는다() {
    UUID unknownId = UUID.randomUUID();
    given(watchingSessionRepository.findById(unknownId)).willReturn(Optional.empty());

    watchingSessionService.leave(unknownId);

    verify(stompBroadcastRelay, never()).broadcast(any(String.class), any(Object.class));
  }
}
