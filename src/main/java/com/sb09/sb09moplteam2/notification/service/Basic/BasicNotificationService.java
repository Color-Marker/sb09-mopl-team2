package com.sb09.sb09moplteam2.notification.service.Basic;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.event.message.NotificationCreatedEvent;
import com.sb09.sb09moplteam2.event.message.NotificationDmEvent;
import com.sb09.sb09moplteam2.event.message.NotificationRoleEvent;
import com.sb09.sb09moplteam2.exception.notification.NotificationForbiddenException;
import com.sb09.sb09moplteam2.exception.notification.NotificationNotFoundException;
import com.sb09.sb09moplteam2.exception.playlist.PlaylistNotFoundException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.exception.websocket.DirectMessageNotFoundException;
import com.sb09.sb09moplteam2.notification.dto.data.NotificationDto;
import com.sb09.sb09moplteam2.notification.dto.request.NotificationListRequest;
import com.sb09.sb09moplteam2.notification.entity.Notification;
import com.sb09.sb09moplteam2.notification.entity.NotificationLevel;
import com.sb09.sb09moplteam2.notification.mapper.CursorResponseNotificationMapper;
import com.sb09.sb09moplteam2.notification.mapper.NotificationMapper;
import com.sb09.sb09moplteam2.notification.repository.NotificationRepository;
import com.sb09.sb09moplteam2.notification.service.NotificationService;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistRepository;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
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
  private final DirectMessageRepository messageRepository;
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
  public void createFollowNotification(UUID userId, UUID followerId) {

    User follower = userRepository.findById(followerId).orElseThrow(() -> UserNotFoundException.withId(followerId));

    String title = follower.getName() + "님이 나를 팔로우했어요.";

    create(userId, title);
  }

  @Override
  public void createFollowWorkNotification(Set<UUID> userIds, UUID followedId, UUID playlistId) {

    User followed = userRepository.findById(followedId).orElseThrow(() -> UserNotFoundException.withId(followedId));
    Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new PlaylistNotFoundException());

    String title = followed.getName() + "님이 플레이리스트를 만들었어요.";
    String content = "[" + playlist.getTitle() + "] " + playlist.getDescription();

    createMany(userIds, title, content);
  }

  @Override
  public void createSubsNotification(UUID userId, UUID subscriberId, UUID playlistId) {

    User subscriber = userRepository.findById(subscriberId).orElseThrow(() -> UserNotFoundException.withId(subscriberId));
    Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new PlaylistNotFoundException());

    String title = subscriber.getName() + "님이 나의 플레이리스트 [" + playlist.getTitle() + "]를 구독했어요.";

    create(userId, title);
  }

  @Override
  public void createSubsWorkNotification(Set<UUID> userIds, UUID playlistId) {

    Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new PlaylistNotFoundException());

    String title = "구독 중인 플레이리스트 [" + playlist.getTitle() + "]가  업데이트됐어요.";

    createMany(userIds, title, null);
  }

  @Override
  public void createRoleUpdateNotification(UUID userId, Role previous, Role now) {
    User user = userRepository.findById(userId).orElseThrow(() -> UserNotFoundException.withId(userId));

    String title = "내 권한이 변경되었어요.";
    String content = "내 권한이 [" + previous.toString() + "]에서 [" + now.toString() + "]로 변경되었어요.";

    Notification notification = new Notification(user, title, content, NotificationLevel.WARNING);

    notificationRepository.save(notification);

    NotificationDto dto = notificationMapper.toDto(notification);
    eventPublisher.publishEvent(new NotificationRoleEvent(dto, Instant.now()));
  }

  @Override
  public void createDmNotification(UUID userId, DirectMessageDto messageDto) {
    User user = userRepository.findById(userId).orElseThrow(() -> UserNotFoundException.withId(userId));
    DirectMessage message = messageRepository.findById(messageDto.id()).orElseThrow(() -> new DirectMessageNotFoundException(messageDto.id()));

    String title = "[DM] " + messageDto.sender().name();
    String content = messageDto.content();

    Notification notification = new Notification(user, message, title, content);
    notificationRepository.save(notification);

    NotificationDto dto = notificationMapper.toDto(notification);
    eventPublisher.publishEvent(new NotificationDmEvent(dto, Instant.now()));
  }

  private void create(UUID receiverId, String title) {
    User receiver = userRepository.findById(receiverId).orElseThrow(() -> UserNotFoundException.withId(receiverId));
    Notification notification = new Notification(receiver, title, null, NotificationLevel.INFO);
    notificationRepository.save(notification);
    NotificationDto dto = notificationMapper.toDto(notification);
    eventPublisher.publishEvent(new NotificationCreatedEvent(List.of(dto), Instant.now()));
  }

  private void createMany(Set<UUID> receiverIds, String title, String content) {
    if (receiverIds.isEmpty()) return;
    List<User> receivers = userRepository.findAllById(receiverIds);
    List<Notification> notifications = receivers.stream()
        .map(receiver -> new Notification(receiver, title, content, NotificationLevel.INFO))
        .toList();
    notificationRepository.saveAll(notifications);
    List<NotificationDto> dtos = notifications.stream()
        .map(notificationMapper::toDto)
        .toList();
    eventPublisher.publishEvent(new NotificationCreatedEvent(dtos, Instant.now()));
  }

}
