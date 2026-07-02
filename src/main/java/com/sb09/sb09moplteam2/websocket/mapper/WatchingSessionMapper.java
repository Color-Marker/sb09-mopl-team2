package com.sb09.sb09moplteam2.websocket.mapper;

import com.sb09.sb09moplteam2.content.service.ContentService;
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
  private final ContentService contentService;

  public WatchingSessionDto toDto(WatchingSession session) {
    UserSummary watcher = userService.getUserSummary(session.getUserId());
    ContentSummary content = contentService.getContentSummary(session.getContentId());

    return new WatchingSessionDto(
        session.getId(),
        session.getStartedAt(),
        watcher,
        content
    );
  }
}
