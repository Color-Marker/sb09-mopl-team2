package com.sb09.sb09moplteam2.notification.dto;

import com.sb09.sb09moplteam2.notification.entity.NotificationLevel;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationLevel level
) {

}
