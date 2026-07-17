package com.sb09.sb09moplteam2.websocket.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionChatResponse(
    UUID senderId,
    String senderName,
    String senderProfileImageUrl,
    String content,
    Instant sentAt
) {
}
