package com.sb09.sb09moplteam2.playlist.mapper;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.entity.ContentType;
import com.sb09.sb09moplteam2.content.mapper.ContentMapper;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistItem;
import com.sb09.sb09moplteam2.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaylistMapperTest {

  @Mock
  private ContentMapper contentMapper;

  @Mock
  private ContentTagRepository contentTagRepository;

  @InjectMocks
  private PlaylistMapper playlistMapper;

  @Test
  void 플레이리스트를_DTO로_변환한다() {
    // given
    UUID ownerId = UUID.randomUUID();
    User owner = mock(User.class);
    given(owner.getId()).willReturn(ownerId);
    given(owner.getName()).willReturn("우디");
    given(owner.getProfileImageUrl()).willReturn("profile.jpg");

    Playlist playlist = mock(Playlist.class);
    UUID playlistId = UUID.randomUUID();
    Instant updatedAt = Instant.now();
    given(playlist.getId()).willReturn(playlistId);
    given(playlist.getOwner()).willReturn(owner);
    given(playlist.getTitle()).willReturn("플레이리스트 제목");
    given(playlist.getDescription()).willReturn("설명");
    given(playlist.getUpdatedAt()).willReturn(updatedAt);
    given(playlist.getSubscriberCount()).willReturn(5L);

    Content content = mock(Content.class);
    UUID contentId = UUID.randomUUID();
    given(content.getId()).willReturn(contentId);

    PlaylistItem item = mock(PlaylistItem.class);
    given(item.getContent()).willReturn(content);

    List<ContentTag> tags = List.of(
        ContentTag.builder().content(content).tag("액션").build()
    );
    given(contentTagRepository.findByContentId(contentId)).willReturn(tags);

    ContentSummary contentSummary = new ContentSummary(
        contentId, ContentType.movie, "영화1", "설명", null, List.of("액션"), 0.0, 0
    );
    given(contentMapper.toContentSummary(content, tags)).willReturn(contentSummary);

    // when
    PlaylistDto result = playlistMapper.toDto(playlist, List.of(item), true);

    // then
    assertThat(result.id()).isEqualTo(playlistId);
    assertThat(result.owner().userId()).isEqualTo(ownerId);
    assertThat(result.owner().name()).isEqualTo("우디");
    assertThat(result.owner().profileImageUrl()).isEqualTo("profile.jpg");
    assertThat(result.title()).isEqualTo("플레이리스트 제목");
    assertThat(result.description()).isEqualTo("설명");
    assertThat(result.updatedAt()).isEqualTo(updatedAt);
    assertThat(result.subscriberCount()).isEqualTo(5L);
    assertThat(result.subscribedByMe()).isTrue();
    assertThat(result.contents()).hasSize(1);
    assertThat(result.contents().get(0)).isEqualTo(contentSummary);
  }

  @Test
  void 콘텐츠가_없는_플레이리스트도_변환된다() {
    // given
    User owner = mock(User.class);
    given(owner.getId()).willReturn(UUID.randomUUID());
    given(owner.getName()).willReturn("우디");
    given(owner.getProfileImageUrl()).willReturn(null);

    Playlist playlist = mock(Playlist.class);
    given(playlist.getId()).willReturn(UUID.randomUUID());
    given(playlist.getOwner()).willReturn(owner);
    given(playlist.getTitle()).willReturn("빈 플레이리스트");
    given(playlist.getDescription()).willReturn("설명");
    given(playlist.getUpdatedAt()).willReturn(Instant.now());
    given(playlist.getSubscriberCount()).willReturn(0L);

    // when
    PlaylistDto result = playlistMapper.toDto(playlist, List.of(), false);

    // then
    assertThat(result.contents()).isEmpty();
    assertThat(result.subscribedByMe()).isFalse();
  }
}