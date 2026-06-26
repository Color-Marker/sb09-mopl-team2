package com.sb09.sb09moplteam2.notification.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {
  CursorResponse<NotificationDto> list(UUID userId, NotificationListRequest request);

  void delete(UUID notificationId, UUID userId);

  void createFollowNotification(User user, User follower);
  void createFollowWorkNotification(Set<User> users, User followed, Playlist playlist);
  void createSubsNotification(User user,User subscriber, Playlist playlist);
  void createSubsWorkNotification(Set<User> users, Playlist playlist);

  void createRoleUpdateNotification(User user, Role previous, Role now);

  void createDmNotification(User user, DirectMessage message);
}
