package com.sb09.sb09moplteam2.notification.service;

import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.dto.response.CursorResponseNotificationDto;
import java.util.UUID;

public interface NotificationService {
  CursorResponseNotificationDto<NotificationDto> list(UUID userId, NotificationListRequest request);

  void delete(UUID notificationId, UUID userId);

  void createEventNotification(User user, );

  void createDmNotification(User user, );
}
