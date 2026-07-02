package com.sb09.sb09moplteam2.redis;

import com.sb09.sb09moplteam2.sse.SseMessage;
import java.util.Set;
import java.util.UUID;

public record RedisPubSubMessage(
    UUID eventId,
    Set<UUID> receiverIds,
    String eventName,
    Object eventData
) {
  public static RedisPubSubMessage from(SseMessage message) {
    return new RedisPubSubMessage(
        message.getEventId(),
        message.getReceiverIds(),
        message.getEventName(),
        message.getEventData()
    );
  }
}
