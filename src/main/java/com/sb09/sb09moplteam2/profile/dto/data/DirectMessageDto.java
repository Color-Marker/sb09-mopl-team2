package com.sb09.sb09moplteam2.profile.dto.data;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(
    UUID id,
    UUID conversationId,
    LocalDateTime createdAt,
    UserSummary sender,
    UserSummary receiver,
    String content
) {}
