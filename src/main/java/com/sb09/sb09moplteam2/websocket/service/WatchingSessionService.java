package com.sb09.sb09moplteam2.websocket.service;

import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;

import com.sb09.sb09moplteam2.websocket.repository.WatchingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchingSessionService {

  private final WatchingSessionRepository watchingSessionRepository;

  // GET /api/users/{watcherId}/watching-sessions
  // 특정 유저의 활성 세션 단건 조회 (없으면 null 반환 - nullable)
  public WatchingSessionDto findActiveByUserId(UUID watcherId) {
    return watchingSessionRepository.findActiveByUserId(watcherId)
        .map(this::toDto)
        .orElse(null);
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
    // TODO: 커서 페이지네이션 쿼리 구현 (QueryDSL 또는 JPQL)
    List<WatchingSession> sessions = watchingSessionRepository.findByContentId(contentId);
    List<WatchingSessionDto> data = sessions.stream()
        .map(this::toDto)
        .toList();

    return new CursorResponse<>(
        data,
        null,       // TODO: nextCursor 계산
        null,       // TODO: nextIdAfter 계산
        false,      // TODO: hasNext 계산
        data.size(),
        sortBy,
        sortDirection
    );
  }

  private WatchingSessionDto toDto(WatchingSession session) {
    // TODO: 팀원 User 도메인 연동 후 실제 UserSummary로 교체
    UserSummary watcher = new UserSummary(
        session.getUserId(),
        null,   // TODO: userName
        null    // TODO: profileImageUrl
    );

    // TODO: 팀원 Content 도메인 연동 후 실제 ContentSummary로 교체
    ContentSummary content = new ContentSummary(
        session.getContentId(),
        null,   // TODO: type
        null,   // TODO: title
        null,   // TODO: description
        null,   // TODO: thumbnailUrl
        null,   // TODO: tags
        0.0,    // TODO: averageRating
        0       // TODO: reviewCount
    );

    return new WatchingSessionDto(
        session.getId(),
        session.getStartedAt(),
        watcher,
        content
    );
  }
}
