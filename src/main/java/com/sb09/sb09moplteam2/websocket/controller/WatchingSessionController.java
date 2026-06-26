package com.sb09.sb09moplteam2.websocket.controller;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.websocket.dto.WatchingSessionDto;
import com.sb09.sb09moplteam2.websocket.service.WatchingSessionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/watching-sessions")
@RequiredArgsConstructor
public class WatchingSessionController {

  private final WatchingSessionService watchingSessionService;

  // GET /api/users/{watcherId}/watching-sessions
  // 특정 유저의 활성 시청 세션 단건 조회 (nullable)
  @GetMapping("/users/{watcherId}/watching-sessions")
  public ResponseEntity<WatchingSessionDto> findByWatcher(
      @PathVariable UUID watcherId
  ) {
    log.info("GET /api/users/{}/watching-sessions", watcherId);
    return ResponseEntity.ok(watchingSessionService.findActiveByUserId(watcherId));
  }


  // GET /api/contents/{contentId}/watching-sessions
  // 특정 콘텐츠의 시청 세션 목록 조회 (커서 페이지네이션)
  @GetMapping("/contents/{contentId}/watching-sessions")
  public ResponseEntity<CursorResponse<WatchingSessionDto>> findByContent(
      @PathVariable UUID contentId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam Integer limit,
      @RequestParam String sortBy,
      @RequestParam String sortDirection
  ) {
    log.info("GET /api/contents/{}/watching-sessions", contentId);
    return ResponseEntity.ok(watchingSessionService.findAllByContentId(
        contentId, cursor, idAfter, limit, sortBy, sortDirection));
  }
}
