package com.sb09.sb09moplteam2.websocket.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageResponse(
    UUID id,
    UUID conversationId,
    UUID senderId,
    String content,
    Instant sentAt
) {
}
