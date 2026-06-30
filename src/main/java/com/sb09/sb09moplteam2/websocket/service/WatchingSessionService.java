package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;

import com.sb09.sb09moplteam2.websocket.entity.WatchingSessionStatus;
import com.sb09.sb09moplteam2.websocket.mapper.WatchingSessionMapper;
import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchingSessionService {

  private final WatchingSessionRepository watchingSessionRepository;
  private final WatchingSessionMapper watchingSessionMapper;

  // GET /api/users/{watcherId}/watching-sessions
  // 특정 유저의 활성 세션 단건 조회 (없으면 null 반환 - nullable)
  public WatchingSessionDto findActiveByUserId(UUID watcherId) {
    log.debug("활성 시청 세션 조회: watcherId={}", watcherId);

    WatchingSessionDto result = watchingSessionRepository
        .findByUserIdAndStatus(watcherId, WatchingSessionStatus.ACTIVE)
        .map(watchingSessionMapper::toDto)
        .orElse(null);

    if (result == null) {
      log.debug("활성 시청 세션 없음: watcherId={}", watcherId);
    }

    return result;
  }

  // GET /api/contents/{contentId}/watching-sessions
  // 특정 콘텐츠의 시청 세션 목록 조회 (커서 페이지네이션)
  public CursorResponse<WatchingSessionDto> findAllByContentId(
      UUID contentId,
      String cursor,
      UUID idAfter,
      int limit,
      String sortBy,
      String sortDirection
  ) {
    log.debug("콘텐츠별 시청 세션 목록 조회 요청: contentId={}, cursor={}, idAfter={}, limit={}",
        contentId, cursor, idAfter, limit);

    Pageable pageable = PageRequest.of(0, limit + 1);
    List<WatchingSession> sessions;

    if (cursor != null && idAfter != null) {
      Instant cursorStartedAt = Instant.parse(cursor);
      sessions = watchingSessionRepository.findByContentIdWithCursor(
          contentId, cursorStartedAt, idAfter, pageable);
    } else {
      sessions = watchingSessionRepository.findByContentId(contentId, pageable);
    }

    boolean hasNext = sessions.size() > limit;
    List<WatchingSession> content = hasNext ? sessions.subList(0, limit) : sessions;

    List<WatchingSessionDto> data = content.stream()
        .map(watchingSessionMapper::toDto)
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      WatchingSession last = content.get(content.size() - 1);
      nextCursor = last.getStartedAt().toString();
      nextIdAfter = last.getId();
    }

    log.debug("콘텐츠별 시청 세션 목록 조회 결과: contentId={}, resultSize={}, hasNext={}",
        contentId, data.size(), hasNext);

    return new CursorResponse<>(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        data.size(),
        sortBy,
        sortDirection
    );
  }
}
