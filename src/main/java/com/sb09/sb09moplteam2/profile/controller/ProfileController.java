package com.sb09.sb09moplteam2.profile.controller;

import com.sb09.sb09moplteam2.profile.controller.api.ProfileApi;
import com.sb09.sb09moplteam2.profile.dto.data.ContentSummary;
import com.sb09.sb09moplteam2.profile.dto.data.UserSummary;
import com.sb09.sb09moplteam2.profile.dto.data.WatchingSessionDto;
import com.sb09.sb09moplteam2.profile.dto.request.UserUpdateRequest;
import com.sb09.sb09moplteam2.profile.dto.response.CursorResponseWatchingSessionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class ProfileController implements ProfileApi {

  @Override
  public ResponseEntity<UserSummary> getUserProfile(UUID userId) {
    UserSummary mockUser = new UserSummary(
        userId,
        "코딩하는_라이언",
        "https://api.dicebear.com/7.x/avataaars/svg?seed=" + userId
    );
    return ResponseEntity.ok(mockUser);
  }

  @Override
  public ResponseEntity<UserSummary> updateProfile(UUID userId, UserUpdateRequest request) {
    UserSummary updatedUser = new UserSummary(
        userId,
        request.name(),
        "https://api.dicebear.com/7.x/avataaars/svg?seed=" + userId
    );
    return ResponseEntity.ok(updatedUser);
  }

  @Override
  public ResponseEntity<CursorResponseWatchingSessionDto> getWatchingSessions(UUID watcherId) {
    UserSummary mockWatcher = new UserSummary(watcherId, "코딩하는_라이언", "url");
    ContentSummary mockContent = new ContentSummary(
        UUID.randomUUID(), "MOVIE", "인터스텔라", "우주 탐험 영화", "thumb-url",
        List.of("SF", "우주"), 4.8, 1500
    );

    WatchingSessionDto mockSession = new WatchingSessionDto(
        UUID.randomUUID(), LocalDateTime.now(), mockWatcher, mockContent
    );

    CursorResponseWatchingSessionDto response = new CursorResponseWatchingSessionDto(
        List.of(mockSession), "next-cursor-string", UUID.randomUUID(),
        false, 1L, "createdAt", "DESC"
    );
    return ResponseEntity.ok(response);
  }
}
