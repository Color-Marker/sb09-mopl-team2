package com.sb09.sb09moplteam2.websocket.dto;


import com.sb09.sb09moplteam2.content.dto.data.ContentSummary;

import com.sb09.sb09moplteam2.dto.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record WatchingSessionDto(
    UUID id,
    Instant createdAt,
    UserSummary watcher,
    ContentSummary content
) {}
