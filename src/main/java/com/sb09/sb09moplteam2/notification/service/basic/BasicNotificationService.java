package com.sb09.sb09moplteam2.notification.service.basic;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.exception.notification.NotificationForbiddenException;
import com.sb09.sb09moplteam2.exception.notification.NotificationNotFoundException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.entity.Notification;
import com.sb09.sb09moplteam2.notification.entity.NotificationLevel;
import com.sb09.sb09moplteam2.notification.mapper.CursorResponseNotificationMapper;
import com.sb09.sb09moplteam2.notification.mapper.NotificationMapper;
import com.sb09.sb09moplteam2.notification.repository.NotificationRepository;
import com.sb09.sb09moplteam2.notification.service.NotificationService;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
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
  private final CursorResponseNotificationMapper cursorMapper;
  private final NotificationMapper notificationMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  @Override
  public CursorResponse<NotificationDto> list(UUID userId,
      NotificationListRequest request) {
    log.debug("알림 목록 조회 시작: receiverId={}", userId);

    if(!userRepository.existsById(userId)){
      throw UserNotFoundException.withId(userId);
    }

    Slice<Notification> slice = notificationRepository.searchNotification(userId, request);
    Long totalCount = notificationRepository.countByReceiver_Id(userId);

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
    String title = follower.getName() + "님이 나를 팔로우했어요.";
    create(user, title, null, NotificationLevel.INFO);
  }

  @Override
  public void createFollowWorkNotification(Set<User> users, User followed, Playlist playlist) {
    String title = followed.getName() + "님이 플레이리스트를 만들었어요.";
    String content = "[" + playlist.getTitle() + "] " + playlist.getDescription();
    createMany(users, title, content, NotificationLevel.INFO);
  }

  @Override
  public void createSubsNotification(User user, User subscriber, Playlist playlist) {
    String title = subscriber.getName() + "님이 나의 플레이리스트 [" + playlist.getTitle() + "]를 구독했어요.";
    create(user, title, null, NotificationLevel.INFO);
  }

  @Override
  public void createSubsWorkNotification(Set<User> users, Playlist playlist) {
    String title = "구독 중인 플레이리스트 [" + playlist.getTitle() + "]가  업데이트됐어요.";
    createMany(users, title, null, NotificationLevel.INFO);
  }

  @Override
  public void createRoleUpdateNotification(User user, Role previous, Role now) {
    String title = "내 권한이 변경되었어요.";
    String content = "내 권한이 [" + previous.toString() + "]에서 [" + now.toString() + "]로 변경되었어요.";
    Notification notification = new Notification(user, title, content, NotificationLevel.WARNING);
    notificationRepository.save(notification);
    NotificationDto dto = notificationMapper.toDto(notification);
    eventPublisher.publishEvent(new NotificationRoleEvent(List.of(dto), Instant.now()));
  }

  @Override
  public void createDmNotification(User user, DirectMessage message) {
    UUID senderId = message.getSenderId();
    User sender = userRepository.findById(senderId).orElseThrow(() -> UserNotFoundException.withId(senderId));
    String title = "[DM] " + sender.getName();
    String content = message.getContent();
    Notification notification = new Notification(user, message, title, content);
    notificationRepository.save(notification);
    NotificationDto dto = notificationMapper.toDto(notification);
    eventPublisher.publishEvent(new NotificationDmEvent(List.of(dto), Instant.now()));
  }

  private void create(User receiver, String title, String content, NotificationLevel level) {
    Notification notification = new Notification(receiver, title, content, level);
    notificationRepository.save(notification);
    NotificationDto dto = notificationMapper.toDto(notification);
    eventPublisher.publishEvent(new NotificationCreatedEvent(List.of(dto), Instant.now()));
  }

  private void createMany(Set<User> receivers, String title, String content, NotificationLevel level) {
    if (receivers.isEmpty()) return;
    List<Notification> notifications = receivers.stream()
        .map(receiver -> new Notification(receiver, title, content, level))
        .toList();
    notificationRepository.saveAll(notifications);
    List<NotificationDto> dtos = notifications.stream()
        .map(notificationMapper::toDto)
        .toList();
    eventPublisher.publishEvent(new NotificationCreatedEvent(dtos, Instant.now()));
  }

}
