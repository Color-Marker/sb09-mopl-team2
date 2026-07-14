package com.sb09.sb09moplteam2.websocket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sb09.sb09moplteam2.dto.CursorResponse;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WatchingSessionServiceTest {

  @Mock
  private WatchingSessionRepository watchingSessionRepository;
  @Mock
  private WatchingSessionMapper watchingSessionMapper;
  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private WatchingSessionService watchingSessionService;

  private UUID watcherId;
  private UUID contentId;
  private WatchingSession activeSession;

  @BeforeEach
  void setUp() {
    watcherId = UUID.randomUUID();
    contentId = UUID.randomUUID();

    activeSession = WatchingSession.create(watcherId, contentId);
    ReflectionTestUtils.setField(activeSession, "id", UUID.randomUUID());
  }

  // ───────────────────────────── findActiveByUserId ─────────────────────────────

  @Test
  void 활성_세션이_있으면_DTO를_반환한다() {
    WatchingSessionDto expectedDto = makeWatchingSessionDto(activeSession);

    given(watchingSessionRepository.findByUserIdAndStatus(watcherId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.of(activeSession));
    given(watchingSessionMapper.toDto(activeSession)).willReturn(expectedDto);

    WatchingSessionDto result = watchingSessionService.findActiveByUserId(watcherId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(activeSession.getId());
  }

  @Test
  void 활성_세션이_없으면_null을_반환한다() {
    given(watchingSessionRepository.findByUserIdAndStatus(watcherId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.empty());

    WatchingSessionDto result = watchingSessionService.findActiveByUserId(watcherId);

    assertThat(result).isNull();
  }

  // ───────────────────────────── findAllByContentId ─────────────────────────────

  @Test
  void 첫_페이지_시청_세션_목록을_정상_조회한다() {
    int limit = 2;
    WatchingSession session2 = WatchingSession.create(UUID.randomUUID(), contentId);
    ReflectionTestUtils.setField(session2, "id", UUID.randomUUID());

    WatchingSessionDto dto1 = makeWatchingSessionDto(activeSession);
    WatchingSessionDto dto2 = makeWatchingSessionDto(session2);

    given(watchingSessionRepository.findByContentId(eq(contentId), any(Pageable.class)))
        .willReturn(List.of(activeSession, session2));
    given(watchingSessionMapper.toDto(activeSession)).willReturn(dto1);
    given(watchingSessionMapper.toDto(session2)).willReturn(dto2);

    CursorResponse<WatchingSessionDto> result = watchingSessionService.findAllByContentId(
        contentId, null, null, limit, "startedAt", "DESCENDING");

    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }

  @Test
  void 커서가_있으면_커서_이후_시청_세션_목록을_조회한다() {
    int limit = 2;
    Instant cursorStartedAt = Instant.now().minusSeconds(60);
    String cursor = cursorStartedAt.toString();
    UUID idAfter = UUID.randomUUID();

    WatchingSessionDto dto = makeWatchingSessionDto(activeSession);

    given(watchingSessionRepository.findByContentIdWithCursor(
        eq(contentId), eq(cursorStartedAt), eq(idAfter), any(Pageable.class)))
        .willReturn(List.of(activeSession));
    given(watchingSessionMapper.toDto(activeSession)).willReturn(dto);

    CursorResponse<WatchingSessionDto> result = watchingSessionService.findAllByContentId(
        contentId, cursor, idAfter, limit, "startedAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void limit보다_결과가_많으면_hasNext가_true이고_nextCursor가_세팅된다() {
    int limit = 1;
    WatchingSession session2 = WatchingSession.create(UUID.randomUUID(), contentId);
    ReflectionTestUtils.setField(session2, "id", UUID.randomUUID());

    WatchingSessionDto dto1 = makeWatchingSessionDto(activeSession);

    given(watchingSessionRepository.findByContentId(eq(contentId), any(Pageable.class)))
        .willReturn(List.of(activeSession, session2)); // limit+1 개 반환
    given(watchingSessionMapper.toDto(activeSession)).willReturn(dto1);

    CursorResponse<WatchingSessionDto> result = watchingSessionService.findAllByContentId(
        contentId, null, null, limit, "startedAt", "DESCENDING");

    assertThat(result.data()).hasSize(1);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotNull();
    assertThat(result.nextIdAfter()).isNotNull();
  }

  // ───────────────────────────── join ─────────────────────────────

  @Test
  void join_기존_활성_세션이_없으면_새_세션을_생성하고_JOIN을_브로드캐스트한다() {
    WatchingSessionDto dto = makeWatchingSessionDto(activeSession);

    given(watchingSessionRepository.findByUserIdAndStatus(watcherId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.empty());
    given(watchingSessionMapper.toDto(any(WatchingSession.class))).willReturn(dto);

    UUID resultId = watchingSessionService.join(contentId, watcherId);

    assertThat(resultId).isNotNull();

    ArgumentCaptor<WatchingSession> sessionCaptor = ArgumentCaptor.forClass(WatchingSession.class);
    verify(watchingSessionRepository, times(1)).save(sessionCaptor.capture());
    WatchingSession savedSession = sessionCaptor.getValue();
    assertThat(savedSession.getUserId()).isEqualTo(watcherId);
    assertThat(savedSession.getContentId()).isEqualTo(contentId);
    assertThat(savedSession.getStatus()).isEqualTo(WatchingSessionStatus.ACTIVE);

    ArgumentCaptor<WatchingSessionEvent> eventCaptor = ArgumentCaptor.forClass(WatchingSessionEvent.class);
    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/sub/contents/" + contentId + "/watch"), eventCaptor.capture());
    assertThat(eventCaptor.getValue().type()).isEqualTo("JOIN");
    assertThat(eventCaptor.getValue().watchingSession()).isEqualTo(dto);
  }

  @Test
  void join_기존_활성_세션이_있으면_종료_후_LEAVE를_브로드캐스트하고_새_세션으로_JOIN을_브로드캐스트한다() {
    WatchingSessionDto existingDto = makeWatchingSessionDto(activeSession);
    WatchingSessionDto newDto = new WatchingSessionDto(
        UUID.randomUUID(), Instant.now(), null, null);

    given(watchingSessionRepository.findByUserIdAndStatus(watcherId, WatchingSessionStatus.ACTIVE))
        .willReturn(Optional.of(activeSession));
    given(watchingSessionMapper.toDto(activeSession)).willReturn(existingDto);
    given(watchingSessionMapper.toDto(any(WatchingSession.class)))
        .willAnswer(invocation -> {
          WatchingSession session = invocation.getArgument(0);
          return session == activeSession ? existingDto : newDto;
        });

    UUID resultId = watchingSessionService.join(contentId, watcherId);

    assertThat(resultId).isNotNull();
    assertThat(activeSession.getStatus()).isEqualTo(WatchingSessionStatus.ENDED);

    // 기존 세션 종료 저장 + 새 세션 저장, 총 2번 save 호출
    verify(watchingSessionRepository, times(2)).save(any(WatchingSession.class));

    ArgumentCaptor<WatchingSessionEvent> eventCaptor = ArgumentCaptor.forClass(WatchingSessionEvent.class);
    verify(messagingTemplate, times(2))
        .convertAndSend(eq("/sub/contents/" + contentId + "/watch"), eventCaptor.capture());

    List<WatchingSessionEvent> events = eventCaptor.getAllValues();
    assertThat(events).hasSize(2);
    assertThat(events.get(0).type()).isEqualTo("LEAVE");
    assertThat(events.get(1).type()).isEqualTo("JOIN");
  }

  // ───────────────────────────── leave ─────────────────────────────

  @Test
  void leave_활성_세션을_종료하고_LEAVE_이벤트를_브로드캐스트한다() {
    WatchingSessionDto dto = makeWatchingSessionDto(activeSession);

    given(watchingSessionRepository.findById(activeSession.getId()))
        .willReturn(Optional.of(activeSession));
    given(watchingSessionMapper.toDto(activeSession)).willReturn(dto);

    watchingSessionService.leave(activeSession.getId());

    assertThat(activeSession.getStatus()).isEqualTo(WatchingSessionStatus.ENDED);

    ArgumentCaptor<WatchingSessionEvent> eventCaptor = ArgumentCaptor.forClass(WatchingSessionEvent.class);
    verify(messagingTemplate, times(1))
        .convertAndSend(eq("/sub/contents/" + contentId + "/watch"), eventCaptor.capture());
    assertThat(eventCaptor.getValue().type()).isEqualTo("LEAVE");
    assertThat(eventCaptor.getValue().watchingSession()).isEqualTo(dto);
  }

  @Test
  void leave_이미_종료된_세션이면_브로드캐스트하지_않는다() {
    activeSession.end();

    given(watchingSessionRepository.findById(activeSession.getId()))
        .willReturn(Optional.of(activeSession));

    watchingSessionService.leave(activeSession.getId());

    verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
  }

  @Test
  void leave_존재하지_않는_세션이면_아무것도_하지_않는다() {
    UUID unknownId = UUID.randomUUID();
    given(watchingSessionRepository.findById(unknownId)).willReturn(Optional.empty());

    watchingSessionService.leave(unknownId);

    verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
  }

  // ───────────────────────────── 헬퍼 메서드 ─────────────────────────────

  private WatchingSessionDto makeWatchingSessionDto(WatchingSession session) {
    return new WatchingSessionDto(
        session.getId(),
        session.getStartedAt(),
        null,  // watcher (UserService 모킹 불필요)
        null   // content (ContentService 모킹 불필요)
    );
  }
}
