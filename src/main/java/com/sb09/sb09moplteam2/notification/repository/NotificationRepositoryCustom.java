package com.sb09.sb09moplteam2.notification.repository;

import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {
  Slice<Notification> searchNotification(UUID receiverId, NotificationListRequest request);
}
