package com.sb09.sb09moplteam2.websocket.mapper;

import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.user.service.UserService;
import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;
import com.sb09.sb09moplteam2.websocket.entity.WatchingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WatchingSessionMapper {

  private final UserService userService;

  public WatchingSessionDto toDto(WatchingSession session) {
    UserSummary watcher = userService.getUserSummary(session.getUserId());

    // TODO: Content 도메인 연동 후 contentService.getContentSummary(session.getContentId())로 교체
    ContentSummary content = new ContentSummary(
        session.getContentId(),
        null,   // type
        null,   // title
        null,   // description
        null,   // thumbnailUrl
        null,   // tags
        0.0,    // averageRating
        0       // reviewCount
    );

    return new WatchingSessionDto(
        session.getId(),
        session.getStartedAt(),
        watcher,
        content
    );
  }
}
