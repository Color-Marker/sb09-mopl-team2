package com.sb09.sb09moplteam2.websocket.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionChatResponse(
    Sender sender,
    String content,
    Instant sentAt
) {
  public record Sender(
      UUID userId,
      String name,
      String profileImageUrl
  ) {}
}
