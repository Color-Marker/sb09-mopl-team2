package com.sb09.sb09moplteam2.notification.service;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {
  CursorResponse<NotificationDto> list(UUID userId, NotificationListRequest request);

  void delete(UUID notificationId, UUID userId);

  void createFollowNotification(UUID userId, UUID followerId);
  void createFollowWorkNotification(Set<UUID> userIds, UUID followedId, UUID playlistId);
  void createSubsNotification(UUID subscriberId, UUID playlistId);
  void createSubsWorkNotification(Set<UUID> userIds, UUID playlistId);

  void createRoleUpdateNotification(UUID userId, Role previous, Role now);

  void createDmNotification(UUID userId, DirectMessageDto messageDto);
}
