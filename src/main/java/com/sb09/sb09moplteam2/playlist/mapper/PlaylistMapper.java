package com.sb09.sb09moplteam2.playlist.mapper;

import com.sb09.sb09moplteam2.content.dto.data.ContentSummary;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.content.mapper.ContentMapper;
import com.sb09.sb09moplteam2.content.repository.ContentTagRepository;
import com.sb09.sb09moplteam2.dto.UserSummary;
import com.sb09.sb09moplteam2.playlist.dto.data.PlaylistDto;
import com.sb09.sb09moplteam2.playlist.entity.Playlist;
import com.sb09.sb09moplteam2.playlist.entity.PlaylistItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaylistMapper {

  private final ContentMapper contentMapper;
  private final ContentTagRepository contentTagRepository;

  public PlaylistDto toDto(Playlist playlist, List<PlaylistItem> items, boolean subscribedByMe) {
    List<ContentSummary> contents = items.stream()
        .map(item -> {
          List<ContentTag> tags = contentTagRepository.findByContentId(item.getContent().getId());
          return contentMapper.toContentSummary(item.getContent(), tags);
        })
        .toList();

    return new PlaylistDto(
        playlist.getId(),
        new UserSummary(
            playlist.getOwner().getId(),
            playlist.getOwner().getName(),
            playlist.getOwner().getProfileImageUrl()
        ),
        playlist.getTitle(),
        playlist.getDescription(),
        playlist.getUpdatedAt(),
        playlist.getSubscriberCount(),
        subscribedByMe,
        contents
    );
  }
}