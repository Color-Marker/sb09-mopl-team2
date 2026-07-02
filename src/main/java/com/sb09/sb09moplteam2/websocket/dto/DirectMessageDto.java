package com.sb09.sb09moplteam2.websocket.dto;


import com.sb09.sb09moplteam2.dto.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record DirectMessageDto(
    UUID id,
    UUID conversationId,
    Instant createdAt,
    UserSummary sender,
    UserSummary receiver,
    String content
) {}
