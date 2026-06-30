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
  private final PlaylistMapper playlistMapper;

  @Transactional
  public PlaylistDto create(PlaylistCreatedRequest request, User currentUser) {
    Playlist playlist = Playlist.builder()
        .title(request.title())
        .description(request.description())
        .owner(currentUser)
        .build();
    playlistRepository.save(playlist);
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlist.getId());
    return playlistMapper.toDto(playlist, items, false);
  }

  // 플레이리스트 단건 조회
  public PlaylistDto findById(UUID playlistId, User currentUser) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다: " + playlistId));
    boolean subscribedByMe = currentUser != null &&
        playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUser.getId());
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    return playlistMapper.toDto(playlist, items, subscribedByMe);
  }

  // 플레이리스트 수정
  @Transactional
  public PlaylistDto update(UUID playlistId, PlaylistUpdateRequest request, User currentUser) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다: " + playlistId));
    if (!playlist.getOwner().getId().equals(currentUser.getId())) {
      throw new IllegalArgumentException("플레이리스트 수정 권한이 없습니다.");
    }
    playlist.update(request.title(), request.description());
    List<PlaylistItem> items = playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId);
    return playlistMapper.toDto(playlist, items, false);
  }

  // 플레이리스트 삭제
  @Transactional
  public void delete(UUID playlistId, User currentUser) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다: " + playlistId));
    if (!playlist.getOwner().getId().equals(currentUser.getId())) {
      throw new IllegalArgumentException("플레이리스트 삭제 권한이 없습니다.");
    }
    playlistRepository.delete(playlist);
  }

  // 플레이리스트에 콘텐츠 추가
  @Transactional
  public void addContent(UUID playlistId, UUID contentId, User currentUser) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다."));
    if (!playlist.getOwner().getId().equals(currentUser.getId())) {
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

  // 콘텐츠 삭제
  @Transactional
  public void removeContent(UUID playlistId, UUID contentId, User currentUser) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다."));
    if (!playlist.getOwner().getId().equals(currentUser.getId())) {
      throw new IllegalArgumentException("권한이 없습니다.");
    }
    playlistItemRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);
  }

  // 구독
  @Transactional
  public void subscribe(UUID playlistId, User currentUser) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new NoSuchElementException("플레이리스트를 찾을 수 없습니다."));
    if (playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUser.getId())) {
      throw new IllegalArgumentException("이미 구독 중입니다.");
    }
    playlistSubscriptionRepository.save(PlaylistSubscription.builder()
        .playlist(playlist)
        .subscriber(currentUser)
        .build());
  }

  // 구독 취소
  @Transactional
  public void unsubscribe(UUID playlistId, User currentUser) {
    if (!playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, currentUser.getId())) {
      throw new IllegalArgumentException("구독 중이 아닙니다.");
    }
    playlistSubscriptionRepository.deleteByPlaylistIdAndSubscriberId(playlistId, currentUser.getId());
  }
}
