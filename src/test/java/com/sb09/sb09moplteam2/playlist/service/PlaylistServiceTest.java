package com.sb09.sb09moplteam2.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.repository.ContentRepository;
import com.sb09.sb09moplteam2.exception.content.ContentNotFoundException;
import com.sb09.sb09moplteam2.exception.content.Duplicate_Content;
import com.sb09.sb09moplteam2.exception.playlist.DuplicateSubscribeException;
import com.sb09.sb09moplteam2.exception.playlist.PlaylistForbiddenException;
import com.sb09.sb09moplteam2.exception.playlist.PlaylistNotFoundException;
import com.sb09.sb09moplteam2.exception.user.UserNotFoundException;
import com.sb09.sb09moplteam2.follow.repository.FollowRepository;
import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistCreatedRequest;
import com.sb09.sb09moplteam2.playlist.dto.request.PlaylistUpdateRequest;
import com.sb09.sb09moplteam2.playlist.dto.response.CursorResponsePlaylistDto;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistItem;
import com.sb09.sb09moplteam2.playlist.mapper.PlaylistMapper;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistItemRepository;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistRepository;
import com.sb09.sb09moplteam2.playlist.repository.PlaylistSubscriptionRepository;
import com.sb09.sb09moplteam2.user.entity.User;
import com.sb09.sb09moplteam2.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

  @Mock
  private PlaylistRepository playlistRepository;
  @Mock
  private PlaylistItemRepository playlistItemRepository;
  @Mock
  private PlaylistSubscriptionRepository playlistSubscriptionRepository;
  @Mock
  private ContentRepository contentRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private PlaylistMapper playlistMapper;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private FollowRepository followRepository;

  @InjectMocks
  private PlaylistService playlistService;


  @Test
  void 플레이리스트_생성에_성공한다() {
    UUID ownerId = UUID.randomUUID();
    User owner = mock(User.class);
    PlaylistCreatedRequest request = new PlaylistCreatedRequest("제목", "설명");
    PlaylistDto dto = mock(PlaylistDto.class);

    given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
    given(playlistItemRepository.findByPlaylistIdOrderByOrderIndex(any()))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(dto);

    PlaylistDto result = playlistService.create(request, ownerId);

    assertThat(result).isEqualTo(dto);
    then(playlistRepository).should().save(any(Playlist.class));
  }

  @Test
  void 존재하지_않는_유저가_생성하면_예외가_발생한다() {
    UUID ownerId = UUID.randomUUID();
    PlaylistCreatedRequest request = new PlaylistCreatedRequest("제목", "설명");
    given(userRepository.findById(ownerId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.create(request, ownerId))
        .isInstanceOf(UserNotFoundException.class);
  }


  @Test
  void 다음_페이지가_없으면_hasNext가_false이다() {
    Playlist playlist = mock(Playlist.class);
    given(playlist.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findPlaylistsWithCursor(
        any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any()))
        .willReturn(List.of(playlist));
    given(playlistItemRepository.findByPlaylistIdInOrderByOrderIndex(anyList()))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(mock(PlaylistDto.class));

    CursorResponsePlaylistDto response = playlistService.findAll(
        null, null, null, null, null, 5, "DESCENDING", "updatedAt", null);

    assertThat(response.hasNext()).isFalse();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.nextIdAfter()).isNull();
  }

  @Test
  void 다음_페이지가_있으면_updatedAt_기준_커서를_반환한다() {
    int limit = 2;
    Playlist p1 = mock(Playlist.class);
    Playlist p2 = mock(Playlist.class);
    Playlist p3 = mock(Playlist.class);
    UUID p2Id = UUID.randomUUID();
    Instant p2UpdatedAt = Instant.now();
    given(p1.getId()).willReturn(UUID.randomUUID());
    given(p2.getId()).willReturn(p2Id);
    given(p2.getUpdatedAt()).willReturn(p2UpdatedAt);

    given(playlistRepository.findPlaylistsWithCursor(
        any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any()))
        .willReturn(List.of(p1, p2, p3));
    given(playlistItemRepository.findByPlaylistIdInOrderByOrderIndex(anyList()))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(mock(PlaylistDto.class));

    CursorResponsePlaylistDto response = playlistService.findAll(
        null, null, null, null, null, limit, "DESCENDING", "updatedAt", null);

    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextCursor()).isEqualTo(p2UpdatedAt.toString());
    assertThat(response.nextIdAfter()).isEqualTo(p2Id);
  }

  @Test
  void 다음_페이지가_있으면_subscriberCount_기준_커서를_반환한다() {
    int limit = 1;
    Playlist p1 = mock(Playlist.class);
    Playlist p2 = mock(Playlist.class);
    UUID p1Id = UUID.randomUUID();
    given(p1.getId()).willReturn(p1Id);
    given(p1.getSubscriberCount()).willReturn(10L);

    given(playlistRepository.findPlaylistsWithCursor(
        any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any()))
        .willReturn(List.of(p1, p2));
    given(playlistItemRepository.findByPlaylistIdInOrderByOrderIndex(anyList()))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(mock(PlaylistDto.class));

    CursorResponsePlaylistDto response = playlistService.findAll(
        null, null, null, null, null, limit, "DESCENDING", "subscriberCount", null);

    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextCursor()).isEqualTo("10");
    assertThat(response.nextIdAfter()).isEqualTo(p1Id);
  }

  @Test
  void 로그인하지_않은_사용자는_구독여부_조회를_하지_않는다() {
    Playlist playlist = mock(Playlist.class);
    given(playlist.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findPlaylistsWithCursor(
        any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any()))
        .willReturn(List.of(playlist));
    given(playlistItemRepository.findByPlaylistIdInOrderByOrderIndex(anyList()))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(mock(PlaylistDto.class));

    playlistService.findAll(null, null, null, null, null, 5, "DESCENDING", "updatedAt", null);

    then(playlistSubscriptionRepository).should(never())
        .findPlaylistIdsBySubscriberIdAndPlaylistIdIn(any(), any());
  }

  @Test
  void 플레이리스트_단건_조회에_성공한다() {
    UUID playlistId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    PlaylistDto dto = mock(PlaylistDto.class);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(dto);

    PlaylistDto result = playlistService.findById(playlistId, null);

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void 존재하지_않는_플레이리스트_조회시_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.findById(playlistId, null))
        .isInstanceOf(PlaylistNotFoundException.class);
  }


  @Test
  void 플레이리스트_수정에_성공한다() {
    UUID playlistId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);
    PlaylistUpdateRequest request = new PlaylistUpdateRequest("새제목", "새설명");
    PlaylistDto dto = mock(PlaylistDto.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(ownerId);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(dto);

    PlaylistDto result = playlistService.update(playlistId, request, ownerId);

    assertThat(result).isEqualTo(dto);
    then(playlist).should().update("새제목", "새설명");
  }

  @Test
  void 수정시_존재하지_않는_플레이리스트면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    PlaylistUpdateRequest request = new PlaylistUpdateRequest("새제목", "새설명");
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.update(playlistId, request, UUID.randomUUID()))
        .isInstanceOf(PlaylistNotFoundException.class);
  }

  @Test
  void 소유자가_아니면_수정시_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);
    PlaylistUpdateRequest request = new PlaylistUpdateRequest("새제목", "새설명");

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    assertThatThrownBy(() -> playlistService.update(playlistId, request, UUID.randomUUID()))
        .isInstanceOf(PlaylistForbiddenException.class);
  }


  @Test
  void 플레이리스트_삭제에_성공한다() {

    UUID playlistId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(ownerId);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    playlistService.delete(playlistId, ownerId);

    then(playlistRepository).should().delete(playlist);
  }

  @Test
  void 삭제시_존재하지_않는_플레이리스트면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.delete(playlistId, UUID.randomUUID()))
        .isInstanceOf(PlaylistNotFoundException.class);
  }

  @Test
  void 소유자가_아니면_삭제시_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    assertThatThrownBy(() -> playlistService.delete(playlistId, UUID.randomUUID()))
        .isInstanceOf(PlaylistForbiddenException.class);
  }

  @Test
  void 플레이리스트에_콘텐츠_추가에_성공한다() {
    UUID playlistId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);
    Content content = mock(Content.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(ownerId);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(playlistItemRepository.existsByPlaylistIdAndContentId(playlistId, contentId))
        .willReturn(false);
    given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
    given(playlistItemRepository.findByPlaylistIdOrderByOrderIndex(playlistId))
        .willReturn(List.of());

    playlistService.addContent(playlistId, contentId, ownerId);

    then(playlistItemRepository).should().save(any(PlaylistItem.class));
  }

  @Test
  void 콘텐츠_추가시_플레이리스트가_없으면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.addContent(playlistId, UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(PlaylistNotFoundException.class);
  }

  @Test
  void 소유자가_아니면_콘텐츠_추가시_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    assertThatThrownBy(() -> playlistService.addContent(playlistId, UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(PlaylistForbiddenException.class);
  }

  @Test
  void 이미_추가된_콘텐츠면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(ownerId);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(playlistItemRepository.existsByPlaylistIdAndContentId(playlistId, contentId))
        .willReturn(true);

    assertThatThrownBy(() -> playlistService.addContent(playlistId, contentId, ownerId))
        .isInstanceOf(Duplicate_Content.class);
  }

  @Test
  void 존재하지_않는_콘텐츠_추가시_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(ownerId);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(playlistItemRepository.existsByPlaylistIdAndContentId(playlistId, contentId))
        .willReturn(false);
    given(contentRepository.findById(contentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.addContent(playlistId, contentId, ownerId))
        .isInstanceOf(ContentNotFoundException.class);
  }

  @Test
  void 플레이리스트에서_콘텐츠_삭제에_성공한다() {
    UUID playlistId = UUID.randomUUID();
    UUID contentId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(ownerId);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    playlistService.removeContent(playlistId, contentId, ownerId);

    then(playlistItemRepository).should().deleteByPlaylistIdAndContentId(playlistId, contentId);
  }

  @Test
  void 콘텐츠_삭제시_플레이리스트가_없으면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.removeContent(playlistId, UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(PlaylistNotFoundException.class);
  }

  @Test
  void 소유자가_아니면_콘텐츠_삭제시_예외가_발생한다() {

    UUID playlistId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User owner = mock(User.class);

    given(playlist.getOwner()).willReturn(owner);
    given(owner.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    assertThatThrownBy(() -> playlistService.removeContent(playlistId, UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(PlaylistForbiddenException.class);
  }

  @Test
  void 플레이리스트_구독에_성공한다() {
    UUID playlistId = UUID.randomUUID();
    UUID subscriberId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User subscriber = mock(User.class);

    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(userRepository.findById(subscriberId)).willReturn(Optional.of(subscriber));
    given(playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, subscriberId))
        .willReturn(false);

    playlistService.subscribe(playlistId, subscriberId);

    then(playlistSubscriptionRepository).should().save(any());
    then(playlist).should().incrementSubscriberCount();
  }

  @Test
  void 구독시_플레이리스트가_없으면_예외가_발생한다() {

    UUID playlistId = UUID.randomUUID();
    given(playlistRepository.findById(playlistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.subscribe(playlistId, UUID.randomUUID()))
        .isInstanceOf(PlaylistNotFoundException.class);
  }

  @Test
  void 구독시_유저가_없으면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    UUID subscriberId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);

    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(userRepository.findById(subscriberId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> playlistService.subscribe(playlistId, subscriberId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 이미_구독한_플레이리스트면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    UUID subscriberId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);
    User subscriber = mock(User.class);

    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));
    given(userRepository.findById(subscriberId)).willReturn(Optional.of(subscriber));
    given(playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, subscriberId))
        .willReturn(true);

    assertThatThrownBy(() -> playlistService.subscribe(playlistId, subscriberId))
        .isInstanceOf(DuplicateSubscribeException.class);
  }

  @Test
  void 플레이리스트_구독_취소에_성공한다() {
    UUID playlistId = UUID.randomUUID();
    UUID subscriberId = UUID.randomUUID();
    Playlist playlist = mock(Playlist.class);

    given(playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, subscriberId))
        .willReturn(true);
    given(playlistRepository.findById(playlistId)).willReturn(Optional.of(playlist));

    playlistService.unsubscribe(playlistId, subscriberId);

    then(playlistSubscriptionRepository).should()
        .deleteByPlaylistIdAndSubscriberId(playlistId, subscriberId);
    then(playlist).should().decrementSubscriberCount();
  }

  @Test
  void 구독하지_않은_상태에서_취소하면_예외가_발생한다() {
    UUID playlistId = UUID.randomUUID();
    UUID subscriberId = UUID.randomUUID();
    given(playlistSubscriptionRepository.existsByPlaylistIdAndSubscriberId(playlistId, subscriberId))
        .willReturn(false);

    assertThatThrownBy(() -> playlistService.unsubscribe(playlistId, subscriberId))
        .isInstanceOf(DuplicateSubscribeException.class);
  }
  @Test
  void 첫_페이지_조회시_totalCount를_계산한다() {
    Playlist playlist = mock(Playlist.class);
    given(playlist.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findPlaylistsWithCursor(
        any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any()))
        .willReturn(List.of(playlist));
    given(playlistItemRepository.findByPlaylistIdInOrderByOrderIndex(anyList()))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(mock(PlaylistDto.class));
    given(playlistRepository.countPlaylists(any(), any(), any()))
        .willReturn(5L);

    CursorResponsePlaylistDto response = playlistService.findAll(
        null, null, null, null, null, 5, "DESCENDING", "updatedAt", null);

    assertThat(response.totalCount()).isEqualTo(5L);
  }

  @Test
  void 다음_페이지_조회시_totalCount를_계산하지_않고_null을_반환한다() {
    Playlist playlist = mock(Playlist.class);
    given(playlist.getId()).willReturn(UUID.randomUUID());
    given(playlistRepository.findPlaylistsWithCursor(
        any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any()))
        .willReturn(List.of(playlist));
    given(playlistItemRepository.findByPlaylistIdInOrderByOrderIndex(anyList()))
        .willReturn(List.of());
    given(playlistMapper.toDto(any(Playlist.class), anyList(), anyBoolean()))
        .willReturn(mock(PlaylistDto.class));

    CursorResponsePlaylistDto response = playlistService.findAll(
        null, null, null, "0", UUID.randomUUID(), 5, "DESCENDING", "updatedAt", null);

    assertThat(response.totalCount()).isNull();
    then(playlistRepository).should(never()).countPlaylists(any(), any(), any());
  }
}