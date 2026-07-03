package com.sb09.sb09moplteam2.content.mapper;

import com.sb09.sb09moplteam2.content.dto.data.ContentDto;
import com.sb09.sb09moplteam2.content.entity.Content;
import com.sb09.sb09moplteam2.content.entity.ContentTag;
import com.sb09.sb09moplteam2.dto.ContentSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentMapper {

  public ContentDto toDto(Content content, List<ContentTag> tags) {
    return new ContentDto(
        content.getId(),
        content.getType(),
        content.getTitle(),
        content.getDescription(),
        content.getThumbnailUrl(),
        tags.stream().map(ContentTag::getTag).toList(),
        content.getAverageRating(),
        content.getReviewCount(),
        content.getWatcherCount()
    );
  }

  public ContentSummary toContentSummary(Content content, List<ContentTag> tags) {
    List<String> tagNames = tags.stream()
        .map(ContentTag::getTag)
        .toList();
    return new ContentSummary(
        content.getId(),
        content.getType(),
        content.getTitle(),
        content.getDescription(),
        content.getThumbnailUrl(),
        tagNames,
        content.getAverageRating(),
        content.getReviewCount()
    );
  }
}