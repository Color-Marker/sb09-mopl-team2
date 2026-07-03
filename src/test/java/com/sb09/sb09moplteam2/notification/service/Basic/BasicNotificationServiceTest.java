package com.sb09.sb09moplteam2.notification.service.Basic;

import com.sb09.sb09moplteam2.dto.CursorResponse;
import com.sb09.sb09moplteam2.dto.UserSummary;
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
import com.sb09.sb09moplteam2.notification.mapper.CursorResponseNotificationMapper;
import com.sb09.sb09moplteam2.notification.mapper.NotificationMapper;
import com.sb09.sb09moplteam2.notification.repository.NotificationRepository;
import com.sb09.sb09moplteam2.notification.service.Basic.BasicNotificationService;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistRepository;
import com.sb09.sb09moplteam2.user.entity.Role;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import com.sb09.sb09moplteam2.websocket.dto.DirectMessageDto;
import com.sb09.sb09moplteam2.websocket.entity.DirectMessage;
import com.sb09.sb09moplteam2.websocket.repository.DirectMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class BasicNotificationServiceTest {

  @InjectMocks
  private BasicNotificationService notificationService;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PlaylistRepository playlistRepository;

  @Mock
  private DirectMessageRepository messageRepository;

  @Mock
  private CursorResponseNotificationMapper cursorMapper;

  @Mock
  private NotificationMapper notificationMapper;

  @Mock
  private ApplicationEventPublisher eventPublisher;


  @Test
  void list_성공() {
    UUID userId = UUID.randomUUID();
    NotificationListRequest request = mock(NotificationListRequest.class);
    Slice<Notification> slice = new SliceImpl<>(Collections.emptyList());
    CursorResponse<NotificationDto> expected = mock(CursorResponse.class);

    given(userRepository.existsById(userId)).willReturn(true);
    given(notificationRepository.searchNotification(userId, request)).willReturn(slice);
    given(notificationRepository.countByReceiver_Id(userId)).willReturn(5L);
    given(cursorMapper.<Notification, NotificationDto>fromSlice(
        eq(slice), any(), any(), any(), eq(5L), any()))
        .willReturn(expected);
    CursorResponse<NotificationDto> result = notificationService.list(userId, request);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void list_존재하지않는유저_UserNotFoundException() {
    UUID userId = UUID.randomUUID();
    NotificationListRequest request = mock(NotificationListRequest.class);

    given(userRepository.existsById(userId)).willReturn(false);

    assertThatThrownBy(() -> notificationService.list(userId, request))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(notificationRepository);
  }


  @Test
  void delete_성공() {
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    User receiver = mock(User.class);
    given(receiver.getId()).willReturn(userId);

    Notification notification = mock(Notification.class);
    given(notification.getReceiver()).willReturn(receiver);
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    notificationService.delete(notificationId, userId);

    verify(notificationRepository).delete(notification);
  }

  @Test
  void delete_존재하지않는알림_NotificationNotFoundException() {
    UUID notificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.delete(notificationId, userId))
        .isInstanceOf(NotificationNotFoundException.class);

    verify(notificationRepository, never()).delete(any());
  }

  @Test
  void delete_다른유저의알림_NotificationForbiddenException() {
    UUID ownerId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    User receiver = mock(User.class);
    given(receiver.getId()).willReturn(ownerId);

    Notification notification = mock(Notification.class);
    given(notification.getReceiver()).willReturn(receiver);
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    assertThatThrownBy(() -> notificationService.delete(notificationId, otherId))
        .isInstanceOf(NotificationForbiddenException.class);

    verify(notificationRepository, never()).delete(any());
  }


  @Test
  void createFollowNotification_성공() {
    UUID userId = UUID.randomUUID();
    UUID followerId = UUID.randomUUID();

    User follower = mock(User.class);
    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));

    User receiver = mock(User.class);
    given(userRepository.findById(userId)).willReturn(Optional.of(receiver));

    given(notificationRepository.save(any())).willReturn(mock(Notification.class));
    given(notificationMapper.toDto(any())).willReturn(mock(NotificationDto.class));

    notificationService.createFollowNotification(userId, followerId);

    verify(notificationRepository).save(any());
    verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
  }

  @Test
  void createFollowNotification_팔로워없음_UserNotFoundException() {
    UUID userId = UUID.randomUUID();
    UUID followerId = UUID.randomUUID();

    given(userRepository.findById(followerId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createFollowNotification(userId, followerId))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(notificationRepository, eventPublisher);
  }

  @Test
  void createFollowNotification_수신자없음_UserNotFoundException() {
    UUID userId = UUID.randomUUID();
    UUID followerId = UUID.randomUUID();

    User follower = mock(User.class);
    given(follower.getName()).willReturn("철수");
    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createFollowNotification(userId, followerId))
        .isInstanceOf(UserNotFoundException.class);

    verify(notificationRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }


  @Test
  void createFollowWorkNotification_성공() {
    UUID followedId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();
    Set<UUID> userIds = Set.of(UUID.randomUUID(), UUID.randomUUID());

    User followed = mock(User.class);
    given(followed.getName()).willReturn("영희");
    given(userRepository.findById(followedId)).willReturn(Optional.of(followed));

    Playlist playlist = mock(Playlist.class);
    given(playlist.getTitle()).willReturn("내 플레이리스트");
    given(playlist.getDescription()).willReturn("설명");
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    List<User> receivers = List.of(mock(User.class), mock(User.class));
    given(userRepository.findAllById(userIds)).willReturn(receivers);
    given(notificationRepository.saveAll(anyList())).willReturn(List.of(mock(Notification.class), mock(Notification.class)));
    given(notificationMapper.toDto(any())).willReturn(mock(NotificationDto.class));

    notificationService.createFollowWorkNotification(userIds, followedId, playlistId);

    verify(notificationRepository).saveAll(anyList());
    verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
  }

  @Test
  void createFollowWorkNotification_수신자목록비어있음_저장없이종료() {
    UUID followedId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();

    User followed = mock(User.class);
    given(followed.getName()).willReturn("영희");
    given(userRepository.findById(followedId)).willReturn(Optional.of(followed));

    Playlist playlist = mock(Playlist.class);
    given(playlist.getTitle()).willReturn("내 플레이리스트");
    given(playlist.getDescription()).willReturn("설명");
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    notificationService.createFollowWorkNotification(Set.of(), followedId, playlistId);

    verify(notificationRepository, never()).saveAll(anyList());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void createFollowWorkNotification_followed없음_UserNotFoundException() {
    UUID followedId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();

    given(userRepository.findById(followedId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createFollowWorkNotification(Set.of(UUID.randomUUID()), followedId, playlistId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void createFollowWorkNotification_플레이리스트없음_PlaylistNotFoundException() {
    UUID followedId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();

    User followed = mock(User.class);
    given(userRepository.findById(followedId)).willReturn(Optional.of(followed));
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createFollowWorkNotification(Set.of(UUID.randomUUID()), followedId, playlistId))
        .isInstanceOf(PlaylistNotFoundException.class);
  }


  @Test
  void createSubsNotification_성공() {
    UUID subscriberId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();

    User subscriber = mock(User.class);
    given(subscriber.getName()).willReturn("민준");
    given(userRepository.findById(subscriberId)).willReturn(Optional.of(subscriber));

    User owner = mock(User.class);
    given(owner.getId()).willReturn(ownerId);

    Playlist playlist = mock(Playlist.class);
    given(playlist.getTitle()).willReturn("힙합 모음");
    given(playlist.getOwner()).willReturn(owner);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    given(userRepository.findById(ownerId)).willReturn(Optional.of(mock(User.class)));

    given(notificationRepository.save(any())).willReturn(mock(Notification.class));
    given(notificationMapper.toDto(any())).willReturn(mock(NotificationDto.class));

    notificationService.createSubsNotification(subscriberId, playlistId);

    verify(notificationRepository).save(any());
    verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
  }

  @Test
  void createSubsNotification_구독자없음_UserNotFoundException() {
    UUID subscriberId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();

    given(userRepository.findById(subscriberId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createSubsNotification(subscriberId, playlistId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void createSubsNotification_플레이리스트없음_PlaylistNotFoundException() {
    UUID subscriberId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();

    User subscriber = mock(User.class);
    given(userRepository.findById(subscriberId)).willReturn(Optional.of(subscriber));
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createSubsNotification(subscriberId, playlistId))
        .isInstanceOf(PlaylistNotFoundException.class);
  }

  @Test
  void createSubsNotification_수신자없음_UserNotFoundException() {
    UUID subscriberId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();

    User subscriber = mock(User.class);
    given(subscriber.getName()).willReturn("민준");
    given(userRepository.findById(subscriberId)).willReturn(Optional.of(subscriber));

    User owner = mock(User.class);
    given(owner.getId()).willReturn(ownerId);

    Playlist playlist = mock(Playlist.class);
    given(playlist.getTitle()).willReturn("힙합 모음");
    given(playlist.getOwner()).willReturn(owner);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    given(userRepository.findById(ownerId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createSubsNotification(subscriberId, playlistId))
        .isInstanceOf(UserNotFoundException.class);
  }
  @Test
  void createSubsWorkNotification_성공() {
    UUID playlistId = UUID.randomUUID();
    Set<UUID> userIds = Set.of(UUID.randomUUID(), UUID.randomUUID());

    Playlist playlist = mock(Playlist.class);
    given(playlist.getTitle()).willReturn("재즈 컬렉션");
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    List<User> receivers = List.of(mock(User.class), mock(User.class));
    given(userRepository.findAllById(userIds)).willReturn(receivers);
    given(notificationRepository.saveAll(anyList())).willReturn(List.of(mock(Notification.class)));
    given(notificationMapper.toDto(any())).willReturn(mock(NotificationDto.class));

    notificationService.createSubsWorkNotification(userIds, playlistId);

    verify(notificationRepository).saveAll(anyList());
    verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
  }

  @Test
  void createSubsWorkNotification_수신자목록비어있음_저장없이종료() {
    UUID playlistId = UUID.randomUUID();

    Playlist playlist = mock(Playlist.class);
    given(playlist.getTitle()).willReturn("재즈 컬렉션");
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    notificationService.createSubsWorkNotification(Set.of(), playlistId);

    verify(notificationRepository, never()).saveAll(anyList());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void createSubsWorkNotification_플레이리스트없음_PlaylistNotFoundException() {
    UUID playlistId = UUID.randomUUID();

    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createSubsWorkNotification(Set.of(UUID.randomUUID()), playlistId))
        .isInstanceOf(PlaylistNotFoundException.class);
  }

  @Test
  void createRoleUpdateNotification_성공() {
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(notificationRepository.save(any())).willReturn(mock(Notification.class));
    given(notificationMapper.toDto(any())).willReturn(mock(NotificationDto.class));

    notificationService.createRoleUpdateNotification(userId, Role.USER, Role.ADMIN);

    verify(notificationRepository).save(any());
    verify(eventPublisher).publishEvent(any(NotificationRoleEvent.class));
    verify(eventPublisher, never()).publishEvent(any(NotificationCreatedEvent.class));
  }

  @Test
  void createRoleUpdateNotification_유저없음_UserNotFoundException() {
    UUID userId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.createRoleUpdateNotification(userId, Role.USER, Role.ADMIN))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(notificationRepository, eventPublisher);
  }


  @Test
  void createDmNotification_성공() {
    UUID userId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();

    User user = mock(User.class);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    DirectMessage message = mock(DirectMessage.class);
    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

    UserSummary sender = mock(UserSummary.class);
    given(sender.name()).willReturn("지훈");

    DirectMessageDto messageDto = new DirectMessageDto(
        messageId,
        UUID.randomUUID(),
        Instant.now(),
        sender,
        mock(UserSummary.class),
        "안녕하세요!"
    );

    given(notificationRepository.save(any())).willReturn(mock(Notification.class));
    given(notificationMapper.toDto(any())).willReturn(mock(NotificationDto.class));

    notificationService.createDmNotification(userId, messageDto);

    verify(notificationRepository).save(any());
    verify(eventPublisher).publishEvent(any(NotificationDmEvent.class));
    verify(eventPublisher, never()).publishEvent(any(NotificationCreatedEvent.class));
  }

  @Test
  void createDmNotification_유저없음_UserNotFoundException() {
    UUID userId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    DirectMessageDto messageDto = new DirectMessageDto(
        messageId,
        UUID.randomUUID(),
        Instant.now(),
        mock(UserSummary.class),
        mock(UserSummary.class),
        "안녕하세요!"
    );

    assertThatThrownBy(() -> notificationService.createDmNotification(userId, messageDto))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(notificationRepository, eventPublisher);
  }

  @Test
  void createDmNotification_메시지없음_DirectMessageNotFoundException() {
    UUID userId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();

    User user = mock(User.class);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    DirectMessageDto messageDto = new DirectMessageDto(
        messageId,
        UUID.randomUUID(),
        Instant.now(),
        mock(UserSummary.class),
        mock(UserSummary.class),
        "안녕하세요!"
    );

    assertThatThrownBy(() -> notificationService.createDmNotification(userId, messageDto))
        .isInstanceOf(DirectMessageNotFoundException.class);

    verify(notificationRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }

}
