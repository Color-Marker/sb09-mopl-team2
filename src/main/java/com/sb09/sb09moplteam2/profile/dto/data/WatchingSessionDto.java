package com.sb09.sb09moplteam2.profile.dto.data;

import java.time.LocalDateTime;
import java.util.UUID;

public record WatchingSessionDto(
    UUID id,
    LocalDateTime createdAt,
    UserSummary watcher,
    ContentSummary content
) {}
