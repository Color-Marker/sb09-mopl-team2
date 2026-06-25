package com.sb09.sb09moplteam2.notification.service.basic;

import com.sb09.sb09moplteam2.exception.notification.NotificationForbiddenException;
import com.sb09.sb09moplteam2.exception.notification.NotificationNotFoundException;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.dto.response.CursorResponseNotificationDto;
import com.sb09.sb09moplteam2.notification.entity.Notification;
import com.sb09.sb09moplteam2.notification.mapper.CursorResponseNotificationMapper;
import com.sb09.sb09moplteam2.notification.mapper.NotificationMapper;
import com.sb09.sb09moplteam2.notification.repository.NotificationRepository;
import com.sb09.sb09moplteam2.notification.service.NotificationService;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final PlaylistRepository playlistRepository;
  private final CursorResponseNotificationMapper cursorMapper;
  private final NotificationMapper notificationMapper;

  @PreAuthorize("principal.userDto.id == #userId")
  @Transactional(readOnly = true)
  @Override
  public CursorResponseNotificationDto<NotificationDto> list(UUID userId,
      NotificationListRequest request) {
    log.debug("알림 목록 조회 시작: receiverId={}", userId);
    User user = userRepository.findById(userId).orElseThrow(
        () -> {
          log.warn("사용자를 찾을 수 없습니다.");
          return UserNotFoundException.withId(userId);
        }
    );

    Slice<Notification> slice = notificationRepository.searchNotification(request);
    Long totalCount = notificationRepository.countByReceiver_Id(userId);

    log.debug("유저ID: {} 의 알림 목록을 불러옵니다.", userId);
    log.debug("유저ID {} 의 알림 {} 개를 불러옵니다.", userId, totalCount);

    return cursorMapper.fromSlice(
        slice,
        notificationMapper::toDto,
        Notification::getCreatedAt,
        Notification::getId,
        totalCount,
        request.getSortDirection()
    );
  }

  @PreAuthorize("principal.userDto.id == #userId")
  @Override
  public void delete(UUID notificationId, UUID userId) {
    log.debug("알림 삭제 시작: id={}, userId={}", notificationId, userId);

    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));

    if (!notification.getReceiver().getId().equals(userId)) {
      log.warn("알림 삭제 권한 없음: id={}, userId={}", notificationId, userId);
      throw NotificationForbiddenException.withId(notificationId, userId);
    }
    notificationRepository.delete(notification);
  }

  @Override
  public void createFollowNotification(User user, User follower) {

  }

  @Override
  public void createFollowWorkNotification(User user, User followed, Playlist playlist) {

  }

  @Override
  public void createSubsNotification(User user, User subscriber, Playlist playlist) {

  }

  @Override
  public void createSubsWorkNotification(User user, Playlist playlist) {

  }

  @Override
  public void createRoleUpdateNotification(User user, Role previous, Role now) {

  }

  @Override
  public void createDmNotification(User user, DirectMessage message) {

  }
}
