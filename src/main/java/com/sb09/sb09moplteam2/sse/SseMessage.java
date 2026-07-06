package com.sb09.sb09moplteam2.sse;

import java.util.Set;
import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record SseMessage(
    UUID eventId,
    Set<UUID> receiverIds,
    String eventName,
    Object eventData
) {
  public boolean isReceivable(UUID receiverId) {
    return receiverIds.contains(receiverId);
  }

  public Set<DataWithMediaType> toEvent() {
    return SseEmitter.event()
        .id(eventId.toString())
        .name(eventName)
        .data(eventData)
        .build();
  }
}
