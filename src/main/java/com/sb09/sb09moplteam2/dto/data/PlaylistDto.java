package com.sb09.sb09moplteam2.dto.data;

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
    List<ContentSummary> contents   // 별도 DTO
) {}