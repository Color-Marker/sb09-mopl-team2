package com.sb09.sb09moplteam2.playlist.dto.data;

import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.dto.UserSummary;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistDto(
    UUID id,
    UserSummary owner,
    String title,
    String description,
    Instant updatedAt,
    Long subscriberCount,
    Boolean subscribedByMe,
    List<ContentSummary> contents
) {}