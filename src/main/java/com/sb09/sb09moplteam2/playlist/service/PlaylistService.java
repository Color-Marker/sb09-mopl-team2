package com.sb09.sb09moplteam2.playlist.service;


import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.event.message.FollowUserWorkEvent;
import com.sb09.sb09moplteam2.exception.content.ContentNotFoundException;
import com.sb09.sb09moplteam2.exception.content.Duplicate_Content;
import com.sb09.sb09moplteam2.event.message.SubsPlaylistWorkEvent;
import com.sb09.sb09moplteam2.event.message.SubscribedPlaylistEvent;
import com.sb09.sb09moplteam2.exception.playlist.DuplicateSubscribeException;
import com.sb09.sb09moplteam2.exception.playlist.PlaylistForbiddenException;
import com.sb09.sb09moplteam2.exception.playlist.PlaylistNotFoundException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.follow.entity.Follow;
import com.sb09.sb09moplteam2.follow.repository.FollowRepository;
import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistCreatedRequest;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistUpdateRequest;
import com.sb09.sb09moplteam2.playlist.dto.response.CursorResponsePlaylistDto;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistItem;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistSubscription;
import com.sb09.sb09moplteam2.playlist.mapper.PlaylistMapper;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistItemRepository;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistRepository;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistSubscriptionRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

  private final PlaylistRepository playlistRepository;
  private final PlaylistItemRepository playlistItemRepository;
  private final PlaylistSubscriptionRepository playlistSubscriptionRepository;
  private final ContentRepository contentRepository;
  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final PlaylistMapper playlistMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public PlaylistDto create(PlaylistCreatedRequest request, UUID ownerId) {
    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> UserNotFoundException.withId(ownerId));
    Playlist playlist = Playlist.builder()
        .title(request.title())
        .description(request.description())
        .owner(owner)
        .build();
    playlistRepository.save(playlist);
    log.info("플레이리스트 생성 완료 - playlistId: {}", playlist.getId());
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlist.getId());

    List<Follow> followList = followRepository.findAllByFollowee_Id(ownerId);
    Set<UUID> followerIds = followList.stream()
        .map(follow -> follow.getFollower().getId())
        .collect(Collectors.toSet());
    eventPublisher.publishEvent(
        new FollowUserWorkEvent(followerIds, ownerId, playlist.getId())
    );

    return playlistMapper.toDto(playlist, items, false);
  }

  @Transactional(readOnly = true)
  public CursorResponsePlaylistDto findAll(
      String keywordLike,
      UUID ownerIdEqual,
      UUID subscriberIdEqual,
      String cursor,
      UUID idAfter,
      int limit,
      String sortDirection,
      String sortBy,
      UUID currentUserId
  ) {
    log.info("플레이리스트 목록 조회 - sortBy: {}, sortDirection: {}, limit: {}", sortBy, sortDirection, limit);
    List<Playlist> playlists = playlistRepository.findPlaylistsWithCursor(
        keywordLike, ownerIdEqual, subscriberIdEqual, cursor, idAfter, limit, sortDirection, sortBy
    );

    boolean hasNext = playlists.size() > limit;
    List<Playlist> content = hasNext ? playlists.subList(0, limit) : playlists;

    List<UUID> playlistIds = content.stream().map(Playlist::getId).toList();

    Map<UUID, List<PlaylistItem>> itemsMap = playlistItemRepository
          .findByPlaylistIdInOrderByOrderIndex(playlistIds).stream()
        .collect(Collectors.groupingBy(item -> item.getPlaylist().getId()));

    Set<UUID> subscribedPlaylistIds = currentUserId != null
        ? new HashSet<>(playlistSubscriptionRepository
        .findPlaylistIdsBySubscriberIdAndPlaylistIdIn(currentUserId, playlistIds))
        : Set.of();

    List<PlaylistDto> data = content.stream()
        .map(playlist -> {
          List<PlaylistItem> items = itemsMap.getOrDefault(playlist.getId(), List.of());
          boolean subscribedByMe = subscribedPlaylistIds.contains(playlist.getId());
          return playlistMapper.toDto(playlist, items, subscribedByMe);
        })
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      Playlist last = content.get(content.size() - 1);
      nextCursor = "updatedAt".equals(sortBy)
          ? last.getUpdatedAt().toString()
          : String.valueOf(last.getSubscriberCount());
      nextIdAfter = last.getId();
    }

    log.info("플레이리스트 목록 조회 완료 - 총 {}개", data.size());
    return new CursorResponsePlaylistDto(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        (long) data.size(),
        sortBy,
        sortDirection
    );
  }

  public PlaylistDto findById(UUID playlistId, UUID currentUserId) {
    log.info("플레이리스트 단건 조회 - playlistId: {}", playlistId);
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> {
          log.warn("플레이리스트 없음 - playlistId: {}", playlistId);
          return new PlaylistNotFoundException();
        });
    boolean subscribedByMe = currentUserId != null &&
        playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUserId);
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    return playlistMapper.toDto(playlist, items, subscribedByMe);
  }

  @Transactional
  public PlaylistDto update(UUID playlistId, PlaylistUpdateRequest request, UUID currentUserId) {
    log.info("플레이리스트 수정 요청 - playlistId: {}", playlistId);
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> {
          log.info("플레이리스트 수정 완료 - playlistId: {}", playlistId);
          return new PlaylistNotFoundException();
        });
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new PlaylistForbiddenException();
    }
    playlist.update(request.title(), request.description());
    log.info("플레이리스트 수정 완료 - playlistId: {}", playlistId);
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    return playlistMapper.toDto(playlist, items, false);
  }

  @Transactional
  public void delete(UUID playlistId, UUID currentUserId) {
    log.info("플레이리스트 삭제 요청 - playlistId: {}", playlistId);
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> {
          log.warn("플레이리스트 없음 - playlistId: {}", playlistId);
          return new PlaylistNotFoundException();
        });
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new PlaylistForbiddenException();
    }
    playlistRepository.delete(playlist);
    log.info("플레이리스트 삭제 완료 - playlistId: {}", playlistId);
  }

  @Transactional
  public void addContent(UUID playlistId, UUID contentId, UUID currentUserId) {
    log.info("플레이리스트 콘텐츠 추가 요청 - playlistId: {}, contentId: {}", playlistId, contentId);
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> {
          log.warn("플레이리스트 없음 - playlistId: {}", playlistId);
          return new PlaylistNotFoundException();
        });
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new PlaylistForbiddenException();
    }
    if (playlistItemRepository.existsByPlaylistIdAndContentId(playlistId, contentId)) {
      throw new Duplicate_Content();
    }
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> {
          log.warn("콘텐츠 없음 - contentId: {}", contentId);
          return new ContentNotFoundException();
        });
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    int nextOrder = items.isEmpty() ? 1 : items.get(items.size() - 1).getOrderIndex() + 1;
    playlistItemRepository.save(PlaylistItem.builder()
        .playlist(playlist)
        .content(content)
        .orderIndex(nextOrder)
        .build());
    log.info("플레이리스트 콘텐츠 추가 완료 - playlistId: {}, contentId: {}", playlistId, contentId);

    Set<UUID> subscriberIds = playlistSubscriptionRepository.findSubscriberIdsByPlaylistId(playlistId);
    eventPublisher.publishEvent(
        new SubsPlaylistWorkEvent(subscriberIds,playlistId)
    );
  }

  @Transactional
  public void removeContent(UUID playlistId, UUID contentId, UUID currentUserId) {
    log.info("플레이리스트 콘텐츠 삭제 요청 - playlistId: {}, contentId: {}", playlistId, contentId);
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> {
          log.warn("플레이리스트 없음 - playlistId: {}", playlistId);
          return new PlaylistNotFoundException();
        });
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new PlaylistForbiddenException();
    }
    playlistItemRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);
    log.info("플레이리스트 콘텐츠 삭제 완료 - playlistId: {}, contentId: {}", playlistId, contentId);
  }

  @Transactional
  public void subscribe(UUID playlistId, UUID currentUserId) {
    log.info("플레이리스트 구독 요청 - playlistId: {}, userId: {}", playlistId, currentUserId);
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> {
          log.warn("플레이리스트 없음 - playlistId: {}", playlistId);
          return new PlaylistNotFoundException();
        });
    User subscriber = userRepository.findById(currentUserId)
        .orElseThrow(() -> UserNotFoundException.withId(currentUserId));
    if (playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUserId)) {
      throw new DuplicateSubscribeException();
    }
    playlistSubscriptionRepository.save(PlaylistSubscription.builder()
        .playlist(playlist)
        .subscriber(subscriber)
        .build());
    playlist.incrementSubscriberCount();
    log.info("플레이리스트 구독 완료 - playlistId: {}, userId: {}", playlistId, currentUserId);

    eventPublisher.publishEvent(
        new SubscribedPlaylistEvent(subscriber.getId(), playlistId)
    );
  }

  @Transactional
  public void unsubscribe(UUID playlistId, UUID currentUserId) {
    log.info("플레이리스트 구독 취소 요청 - playlistId: {}, userId: {}", playlistId, currentUserId);
    if (!playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUserId)) {
      throw new DuplicateSubscribeException();
    }
    playlistSubscriptionRepository.deleteByPlaylistIdAndSubscriberId(playlistId, currentUserId);

    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> {
          log.warn("플레이리스트 없음 - playlistId: {}", playlistId);
         return new PlaylistNotFoundException();
        });
    playlist.decrementSubscriberCount();
    log.info("플레이리스트 구독 취소 완료 - playlistId: {}, userId: {}", playlistId, currentUserId);
  }
}

