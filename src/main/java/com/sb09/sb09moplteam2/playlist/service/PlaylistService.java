package com.sb09.sb09moplteam2.playlist.service;


import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistCreatedRequest;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistUpdateRequest;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistItem;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistSubscription;
import com.sb09.sb09moplteam2.playlist.mapper.PlaylistMapper;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistItemRepository;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistRepository;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistSubscriptionRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

  private final PlaylistRepository playlistRepository;
  private final PlaylistItemRepository playlistItemRepository;
  private final PlaylistSubscriptionRepository playlistSubscriptionRepository;
  private final ContentRepository contentRepository;
  private final UserRepository userRepository;
  private final PlaylistMapper playlistMapper;

  @Transactional
  public PlaylistDto create(PlaylistCreatedRequest request, UUID ownerId) {
    User owner = userRepository.findById(ownerId)
        .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));
    Playlist playlist = Playlist.builder()
        .title(request.title())
        .description(request.description())
        .owner(owner)
        .build();
    playlistRepository.save(playlist);
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlist.getId());
    return playlistMapper.toDto(playlist, items, false);
  }

  public PlaylistDto findById(UUID playlistId, UUID currentUserId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다: " + playlistId));
    boolean subscribedByMe = currentUserId != null &&
        playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUserId);
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    return playlistMapper.toDto(playlist, items, subscribedByMe);
  }

  @Transactional
  public PlaylistDto update(UUID playlistId, PlaylistUpdateRequest request, UUID currentUserId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다: " + playlistId));
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new IllegalArgumentException("플레이리스트 수정 권한이 없습니다.");
    }
    playlist.update(request.title(), request.description());
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    return playlistMapper.toDto(playlist, items, false);
  }

  @Transactional
  public void delete(UUID playlistId, UUID currentUserId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다: " + playlistId));
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new IllegalArgumentException("플레이리스트 삭제 권한이 없습니다.");
    }
    playlistRepository.delete(playlist);
  }

  @Transactional
  public void addContent(UUID playlistId, UUID contentId, UUID currentUserId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다."));
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    if (playlistItemRepository.existsByPlaylistIdAndContentId(playlistId, contentId)) {
      throw new IllegalArgumentException("이미 추가된 콘텐츠입니다.");
    }
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new NoSuchElementException("콘텐츠를 찾을 수 없습니다."));
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    int nextOrder = items.isEmpty() ? 1 : items.get(items.size() - 1).getOrderIndex() + 1;
    playlistItemRepository.save(PlaylistItem.builder()
        .playlist(playlist)
        .content(content)
        .orderIndex(nextOrder)
        .build());
  }

  @Transactional
  public void removeContent(UUID playlistId, UUID contentId, UUID currentUserId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다."));
    if (!playlist.getOwner().getId().equals(currentUserId)) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    playlistItemRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);
  }

  @Transactional
  public void subscribe(UUID playlistId, UUID currentUserId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다."));
    User subscriber = userRepository.findById(currentUserId)
        .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));
    if (playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUserId)) {
      throw new IllegalArgumentException("이미 구독 중입니다.");
    }
    playlistSubscriptionRepository.save(PlaylistSubscription.builder()
        .playlist(playlist)
        .subscriber(subscriber)
        .build());
  }

  @Transactional
  public void unsubscribe(UUID playlistId, UUID currentUserId) {
    if (!playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUserId)) {
      throw new IllegalArgumentException("구독 중이 아닙니다.");
    }
    playlistSubscriptionRepository.deleteByPlaylistIdAndSubscriberId(playlistId, currentUserId);
  }
}

