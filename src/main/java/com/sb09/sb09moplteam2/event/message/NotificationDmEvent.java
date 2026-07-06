package com.sb09.sb09moplteam2.event.message;

import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import java.time.Instant;

public class NotificationDmEvent extends CreatedEvent<NotificationDto>{

  public NotificationDmEvent(NotificationDto data, Instant createdAt) {
    super(data, createdAt);
  }
}
