package com.sb09.sb09moplteam2.event.message;


import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import java.time.Instant;
import java.util.List;

public class NotificationCreatedEvent extends CreatedEvent<List<NotificationDto>> {

  public NotificationCreatedEvent(List<NotificationDto> data, Instant createdAt) {
    super(data, createdAt);
  }
}
